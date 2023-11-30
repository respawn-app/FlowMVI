package pro.respawn.flowmvi.store

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.api.Recoverable
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.modules.ActionModule
import pro.respawn.flowmvi.modules.IntentModule
import pro.respawn.flowmvi.modules.StateModule
import pro.respawn.flowmvi.modules.SubscribersModule
import pro.respawn.flowmvi.modules.actionModule
import pro.respawn.flowmvi.modules.intentModule
import pro.respawn.flowmvi.modules.launchPipeline
import pro.respawn.flowmvi.modules.pluginModule
import pro.respawn.flowmvi.modules.stateModule
import pro.respawn.flowmvi.modules.subscribersModule

internal class StoreImpl<S : MVIState, I : MVIIntent, A : MVIAction>(
    private val config: StoreConfiguration<S, I, A>,
) : Store<S, I, A>,
    Provider<S, I, A>,
    Recoverable<S, I, A>,
    StorePlugin<S, I, A> by pluginModule(config.plugins), // store is a plugin to itself that manages other plugins
    SubscribersModule by subscribersModule(),
    StateModule<S> by stateModule(config.initial),
    IntentModule<I> by intentModule(config.parallelIntents, config.intentCapacity, config.onOverflow),
    ActionModule<A> by actionModule(config.actionShareBehavior) {

    private var launchJob = atomic<Job?>(null)
    override val name by config::name

    override fun start(scope: CoroutineScope): Job = launchPipeline(
        name = name,
        parent = scope + config.coroutineContext,
        onAction = {
            catch { onAction(it)?.let { this@StoreImpl.action(it) } }
        },
        onTransformState = { transform ->
            catch {
                this@StoreImpl.updateState {
                    onState(this, transform()) ?: this
                }
            }
        },
        onStop = {
            checkNotNull(launchJob.getAndSet(null)) { "Store is closed but was not started" }
            onStop(it)
        },
        onStart = pipeline@{
            check(launchJob.getAndSet(coroutineContext.job) == null) { "Store is already started" }
            launch intents@{
                // run onStart plugins first to not let subscribers appear before the store is started fully
                catch { onStart() }
                observeSubscribers(
                    onSubscribe = { catch { onSubscribe(it) } },
                    onUnsubscribe = { catch { onUnsubscribe(it) } }
                )
                // suspend until store is closed, handling intents
                awaitIntents {
                    catch { check(onIntent(it) == null || !config.debuggable) { UnhandledIntentMessage } }
                }
            }
        }
    )

    @OptIn(DelicateStoreApi::class)
    override suspend fun PipelineContext<S, I, A>.recover(e: Exception) {
        if (coroutineContext[Recoverable] != null) throw e
        withContext(this@StoreImpl) { // add recoverable to the context
            onException(e)?.let { throw it }
        }
    }

    override fun CoroutineScope.subscribe(
        block: suspend Provider<S, I, A>.() -> Unit
    ): Job = launch {
        newSubscriber()
        block(this@StoreImpl)
        check(!config.debuggable) { NonSuspendingSubscriberMessage }
    }.apply {
        invokeOnCompletion { removeSubscriber() }
    }

    override fun close() {
        // completion handler will cleanup the pipeline later
        launchJob.value?.cancel()
    }

    override fun hashCode() = name?.hashCode() ?: super.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is Store<*, *, *>) return false
        if (other.name == null && name == null) return other === this
        return name == other.name
    }

    companion object {

        const val NonSuspendingSubscriberMessage = """
You have subscribed to the store, but your subscribe() block has returned early (without throwing a
CancellationException). When you subscribe, make sure to continue collecting values from the store until the Job 
Returned from the subscribe() is cancelled as you likely don't want to stop being subscribed to the store
(i.e. complete the subscription job on your own).
        """

        const val UnhandledIntentMessage = """
An intent has not been handled after calling all plugins. 
You likely don't want this to happen because intents are supposed to be acted upon.
Make sure you have at least one plugin that handles intents, such as reducePlugin().
        """
    }
}
