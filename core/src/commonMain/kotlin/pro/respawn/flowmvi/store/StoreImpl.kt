package pro.respawn.flowmvi.store

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.MutableStore
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.api.Recoverable
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.SubscriberContext
import pro.respawn.flowmvi.dsl.StoreConfiguration
import pro.respawn.flowmvi.dsl.pipeline
import pro.respawn.flowmvi.modules.ActionModule
import pro.respawn.flowmvi.modules.IntentModule
import pro.respawn.flowmvi.modules.StateModule
import pro.respawn.flowmvi.modules.actionModule
import pro.respawn.flowmvi.modules.intentModule
import pro.respawn.flowmvi.modules.stateModule
import pro.respawn.flowmvi.plugins.CompositePlugin
import kotlin.coroutines.coroutineContext

internal class StoreImpl<S : MVIState, I : MVIIntent, A : MVIAction>(
    private val config: StoreConfiguration<S, I, A>,
    private val actionModule: ActionModule<A> = actionModule(config.actionShareBehavior),
    private val intentModule: IntentModule<I> = intentModule(config.intentCapacity, config.onOverflow),
    private val stateModule: StateModule<S> = stateModule(config.initial),
) : MutableStore<S, I, A>,
    Provider<S, I, A>,
    Recoverable,
    StateModule<S> by stateModule,
    IntentModule<I> by intentModule,
    ActionModule<A> by actionModule {

    override val name by config::name
    override val initial by config::initial
    override operator fun get(pluginName: String) = config.plugin[pluginName]
    private var subscriberCount by atomic(0)
    private var started = atomic(false)

    override suspend fun recover(e: Exception) = withPipeline {
        plugin { onException(e)?.let { throw it } }
    }

    override suspend fun send(action: A) = withPipeline {
        plugin { onAction(action) }?.let { actionModule.send(it) }
    }

    override suspend fun updateState(transform: suspend S.() -> S) = withPipeline {
        stateModule.updateState state@{
            plugin { onState(this@state, transform()) } ?: this
        }
    }

    override fun start(scope: CoroutineScope) = pipeline(scope) {
        require(!started.getAndSet(true)) { "Store is already started" }
        plugin { onStart() }
        while (scope.isActive) {
            plugin { onIntent(receive()) }
            yield()
        }
    }.apply {
        invokeOnCompletion {
            started.getAndSet(false)
            plugin { onStop() }
        }
    }

    override fun CoroutineScope.subscribe(block: SubscriberContext<S, I, A>.() -> Unit) = pipeline(this) {
        ++subscriberCount
        if (subscriberCount == 1) plugin { onSubscribe() }
        SubscriberContext(this@StoreImpl, this@subscribe).run(block)
        awaitCancellation()
    }.apply {
        invokeOnCompletion { --subscriberCount }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend inline fun <T> withPipeline(block: PipelineContext<S, I, A>.() -> T) {
        val pipeline = requireNotNull(coroutineContext[PipelineContext.Key]) as PipelineContext<S, I, A>
        with(pipeline, block)
    }

    private inline fun <T> plugin(block: CompositePlugin<S, I, A>.() -> T) = with(config.plugin, block)

    override fun hashCode() = name.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is Store<*, *, *>) return false
        return name == other.name
    }
}
