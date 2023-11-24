package pro.respawn.flowmvi.store

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.modules.ActionModule
import pro.respawn.flowmvi.modules.IntentModule
import pro.respawn.flowmvi.modules.PipelineModule
import pro.respawn.flowmvi.modules.StateModule
import pro.respawn.flowmvi.modules.actionModule
import pro.respawn.flowmvi.modules.catch
import pro.respawn.flowmvi.modules.intentModule
import pro.respawn.flowmvi.modules.launchPipeline
import pro.respawn.flowmvi.modules.pluginModule
import pro.respawn.flowmvi.modules.stateModule

internal class StoreImpl<S : MVIState, I : MVIIntent, A : MVIAction>(
    private val config: StoreConfiguration<S, I, A>,
) : Store<S, I, A>,
    Provider<S, I, A>,
    PipelineModule<S, I, A>,
    StateModule<S> by stateModule(config.initial),
    IntentModule<I> by intentModule(config.parallelIntents, config.intentCapacity, config.onOverflow),
    ActionModule<A> by actionModule(config.actionShareBehavior) {

    override val name by config::name
    private val pluginModule = pluginModule(config.plugins)
    private var subscriberCount by atomic(0)
    private val pipeline = MutableStateFlow<PipelineContext<S, I, A>?>(null)

    override fun start(scope: CoroutineScope): Job = launchPipeline(
        name = name,
        parent = scope + config.coroutineContext,
        onStop = {
            checkNotNull(pipeline.getAndUpdate { null }) { "Store is closed but was not started" }.close()
            pluginModule.onStop(it)
        }
    ) {
        check(pipeline.compareAndSet(null, this)) { "Store is already started" }
        launch {
            plugin { onStart() }
            awaitIntents {
                plugin {
                    check(onIntent(it) == null || !config.debuggable) { UnhandledIntentMessage }
                }
            }
        }
    }

    override suspend fun PipelineContext<S, I, A>.recover(e: Exception): Unit = with(pluginModule) {
        withContext(this@StoreImpl) { // add Recoverable to the context
            onException(e)?.let { throw it }
        }
    }

    override suspend fun PipelineContext<S, I, A>.onAction(action: A) =
        plugin { onAction(action)?.let { this@StoreImpl.action(it) } }

    override suspend fun PipelineContext<S, I, A>.onTransformState(transform: suspend S.() -> S) = plugin {
        this@StoreImpl.updateState {
            onState(this, transform()) ?: this
        }
    }

    override fun CoroutineScope.subscribe(
        block: suspend Provider<S, I, A>.() -> Unit
    ): Job = launch {
        // await the next available pipeline
        with(pipeline.filterNotNull().first()) {
            try {
                // use the parent scope to not handle exceptions in subscriber's block
                // but add the pipeline to the coroutine's context to retrieve as needed
                plugin { onSubscribe(this@launch, subscriberCount) }
                ++subscriberCount
                block(this@StoreImpl)
                check(!config.debuggable) { NonSuspendingSubscriberMessage }
            } finally {
                plugin { onUnsubscribe(--subscriberCount) }
            }
        }
    }

    override fun close() {
        // completion handler will cleanup the pipeline later
        pipeline.value?.close()
    }

    override fun hashCode() = name?.hashCode() ?: super.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is Store<*, *, *>) return false
        if (other.name == null && name == null) return other === this
        return name == other.name
    }

    @OptIn(DelicateStoreApi::class)
    private suspend inline fun PipelineContext<S, I, A>.plugin(block: StorePlugin<S, I, A>.() -> Unit) =
        catch(this) { pluginModule.block() }

    companion object {

        const val NonSuspendingSubscriberMessage = """
You have subscribed to the store, but your subscribe() block has returned early (without throwing a
CancellationException). When you subscribe, make sure to continue collecting values from the store until the Job 
Returned from the subscribe() is cancelled as you likely don't want to stop being subscribed to the store.
        """

        const val UnhandledIntentMessage = """
An intent has not been handled after calling all plugins. 
You likely don't want this to happen because intents are supposed to be acted upon.
Make sure you have at least one plugin that handles intents, such as reducePlugin().
        """
    }
}
