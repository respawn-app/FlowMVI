@file:Suppress("Deprecation") // wil be removed
package pro.respawn.flowmvi.store

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.MutableStore
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.StoreConfiguration
import pro.respawn.flowmvi.dsl.pipeline
import pro.respawn.flowmvi.modules.ActionModule
import pro.respawn.flowmvi.modules.IntentModule
import pro.respawn.flowmvi.modules.Recoverable
import pro.respawn.flowmvi.modules.StateModule
import pro.respawn.flowmvi.modules.actionModule
import pro.respawn.flowmvi.modules.intentModule
import pro.respawn.flowmvi.modules.stateModule
import pro.respawn.flowmvi.plugins.CompositePlugin
import kotlin.coroutines.coroutineContext

internal class StoreImpl<S : MVIState, I : MVIIntent, A : MVIAction>(
    private val config: StoreConfiguration<S, I, A>,
    private val actionModule: ActionModule<A> = actionModule(config.actionShareBehavior),
    private val stateModule: StateModule<S> = stateModule(config.initial),
    private val intentModule: IntentModule<I> = intentModule(
        config.parallelIntents,
        config.intentCapacity,
        config.onOverflow
    ),
) : MutableStore<S, I, A>,
    Provider<S, I, A>,
    Recoverable<S, I, A>,
    StateModule<S> by stateModule,
    IntentModule<I> by intentModule,
    ActionModule<A> by actionModule {

    override val name by config::name
    override val initial by config::initial
    private var subscriberCount by atomic(0)
    private var pipeline = atomic<PipelineContext<S, I, A>?>(null)

    override fun PipelineContext<S, I, A>.recover(e: Exception): Unit = with(config.plugin) {
        // add Recoverable to the coroutine context
        // and handle the exception asynchronously to allow suspending inside onException
        launch(this@StoreImpl) { onException(e)?.let { throw it } }
    }

    override suspend fun send(action: A): Unit = withPipeline {
        plugin { onAction(action)?.let { actionModule.send(it) } }
    }

    override suspend fun updateState(transform: suspend S.() -> S) = withPipeline {
        with(config.plugin) {
            stateModule.updateState state@{
                onState(this@state, transform()) ?: this
            }
        }
    }

    override fun start(scope: CoroutineScope): Job = pipeline(
        name = name,
        parent = scope,
        onClose = {
            require(pipeline.getAndSet(null) != null) { "Store is closed but was not started" }
            config.plugin.onStop(it)
        },
        onStart = {
            require(pipeline.getAndSet(this) == null) { "Store is already started" }
            launch {
                plugin { onStart() }
                awaitIntents {
                    plugin { onIntent(it) }
                }
            }.invokeOnCompletion {
                // propagate exceptions in this child job to not leave the store in an undefined state
                it?.let { throw it }
            }
        }
    )

    override fun CoroutineScope.subscribe(
        block: suspend Provider<S, I, A>.() -> Unit
    ): Job {
        val pipeline = requireNotNull(pipeline.value) { SubscribedToStoppedStoreMessage }
        // use the parent scope to not handle exceptions in subscriber's block
        // but add the pipeline to the coroutine's context to retrieve as needed
        return launch(pipeline) {
            with(pipeline) { plugin { onSubscribe(subscriberCount) } }
            ++subscriberCount
            block(this@StoreImpl)
            if (config.debuggable) error(NonSuspendingSubscriberMessage)
        }.apply {
            invokeOnCompletion { --subscriberCount }
        }
    }

    @OptIn(DelicateStoreApi::class)
    @Suppress("UNCHECKED_CAST")
    private suspend inline fun <T> withPipeline(block: PipelineContext<S, I, A>.() -> T): T {
        val pipeline = checkNotNull(
            coroutineContext[PipelineContext]
        ) { InvalidContextMessage } as PipelineContext<S, I, A>
        return with(pipeline, block)
    }

    private inline fun PipelineContext<S, I, A>.plugin(block: CompositePlugin<S, I, A>.() -> Unit) = try {
        with(config.plugin, block)
    } catch (e: CancellationException) {
        throw e
    } catch (expected: Exception) {
        recover(expected)
    }

    override fun hashCode() = name.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is Store<*, *, *>) return false
        return name == other.name
    }

    override fun close() {
        (pipeline.getAndSet(null) as? CoroutineScope)?.cancel()
    }

    companion object {

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

        const val SubscribedToStoppedStoreMessage = """
You have tried to subscribe to a store that was not started. This makes no sense as you will not get any updates.
Please start the store first before subscribing.
"""
    }
}
