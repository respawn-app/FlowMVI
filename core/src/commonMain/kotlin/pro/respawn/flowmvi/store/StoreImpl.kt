package pro.respawn.flowmvi.store

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.yield
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.MutableStore
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.api.Recoverable
import pro.respawn.flowmvi.api.Store
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

@OptIn(ExperimentalStdlibApi::class)
internal class StoreImpl<S : MVIState, I : MVIIntent, A : MVIAction>(
    private val config: StoreConfiguration<S, I, A>,
    private val actionModule: ActionModule<A> = actionModule(config.actionShareBehavior),
    private val intentModule: IntentModule<I> = intentModule(config.intentCapacity, config.onOverflow),
    private val stateModule: StateModule<S> = stateModule(config.initial),
) : MutableStore<S, I, A>,
    Provider<S, I, A>,
    Recoverable,
    AutoCloseable,
    StateModule<S> by stateModule,
    IntentModule<I> by intentModule,
    ActionModule<A> by actionModule {

    override val name by config::name
    override val initial by config::initial
    private var subscriberCount by atomic(0)
    private var launchJob = atomic<Job?>(null)

    override suspend fun recover(e: Exception): Unit = withPipeline {
        plugin { onException(e)?.let { throw it } }
    }

    override suspend fun send(action: A): Unit = withPipeline {
        plugin { onAction(action) }?.let { actionModule.send(it) }
    }

    override suspend fun updateState(transform: suspend S.() -> S) = withPipeline {
        stateModule.updateState state@{
            plugin { onState(this@state, transform()) } ?: this
        }
    }

    override fun start(scope: CoroutineScope) = pipeline(scope) {
        require(launchJob.getAndSet(job) == null) { "Store is already started" }
        plugin { onStart() }
        while (scope.isActive) {
            plugin { onIntent(receive()) }
            yield()
        }
    }.apply {
        invokeOnCompletion {
            launchJob.getAndSet(null)?.cancel()
            plugin { onStop() }
        }
    }

    override fun CoroutineScope.subscribe(block: suspend Provider<S, I, A>.() -> Unit) = pipeline(this) {
        plugin { onSubscribe(subscriberCount) }
        ++subscriberCount
        block()
        error(NonSuspendingSubscriberMessage)
    }.apply {
        invokeOnCompletion { --subscriberCount }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend inline fun <T> withPipeline(block: PipelineContext<S, I, A>.() -> T): T {
        val pipeline = checkNotNull(
            coroutineContext[PipelineContext.Key]
        ) { InvalidContextMessage } as PipelineContext<S, I, A>
        return with(pipeline, block)
    }

    private inline fun <T> plugin(block: CompositePlugin<S, I, A>.() -> T) = with(config.plugin, block)
    override fun hashCode() = name.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is Store<*, *, *>) return false
        return name == other.name
    }

    override fun close() {
        launchJob.getAndSet(null)?.cancel()
    }

    private companion object {

        const val NonSuspendingSubscriberMessage = """
You have subscribed to the store, but your subscribe() block has returned early (without throwing a
CancellationException). When you subscribe, make sure to continue collecting values from the store until the Job 
Returned from the subscribe() is cancelled as you likely don't want to stop being subscribed to the store.
        """

        const val InvalidContextMessage = """
You have overridden the CoroutineContext associated to the store in one of the coroutines, but then attempted to use it.
This is not allowed because you lose the ability to call store functions in your plugins/jobs.
Please amend the store context instead. Example:
withContext(Dispatchers.Default) - wrong
withContext(this + Dispatchers.Default) - correct
        """
    }
}
