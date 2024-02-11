package pro.respawn.flowmvi.store

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.exceptions.NonSuspendingSubscriberException
import pro.respawn.flowmvi.exceptions.UnhandledIntentException
import pro.respawn.flowmvi.exceptions.UnhandledStoreException
import pro.respawn.flowmvi.modules.ActionModule
import pro.respawn.flowmvi.modules.IntentModule
import pro.respawn.flowmvi.modules.RecoverModule
import pro.respawn.flowmvi.modules.StateModule
import pro.respawn.flowmvi.modules.SubscribersModule
import pro.respawn.flowmvi.modules.actionModule
import pro.respawn.flowmvi.plugins.compositePlugin
import pro.respawn.flowmvi.modules.intentModule
import pro.respawn.flowmvi.modules.launchPipeline
import pro.respawn.flowmvi.modules.observeSubscribers
import pro.respawn.flowmvi.modules.stateModule
import pro.respawn.flowmvi.modules.subscribersModule

internal class StoreImpl<S : MVIState, I : MVIIntent, A : MVIAction>(
    private val config: StoreConfiguration<S, I, A>,
) : Store<S, I, A>,
    Provider<S, I, A>,
    RecoverModule<S, I, A>,
    StorePlugin<S, I, A> by compositePlugin(config.plugins),
    SubscribersModule by subscribersModule(),
    StateModule<S> by stateModule(config.initial),
    IntentModule<I> by intentModule(config.parallelIntents, config.intentCapacity, config.onOverflow),
    ActionModule<A> by actionModule(config.actionShareBehavior) {

    private var launchJob = atomic<Job?>(null)
    override val name by config::name

    override fun start(scope: CoroutineScope): Job = launchPipeline(
        name = name,
        parent = scope + config.coroutineContext,
        onAction = { catch { onAction(it)?.let { this@StoreImpl.action(it) } } },
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
        }
    ) pipeline@{
        check(launchJob.getAndSet(coroutineContext.job) == null) { "Store is already started" }
        launch intents@{
            coroutineScope {
                // run onStart plugins first to not let subscribers appear before the store is started fully
                catch { onStart() }
                launch {
                    observeSubscribers(
                        onSubscribe = { catch { onSubscribe(it) } },
                        onUnsubscribe = { catch { onUnsubscribe(it) } }
                    )
                }
                launch {
                    awaitIntents {
                        catch { if (onIntent(it) != null && config.debuggable) throw UnhandledIntentException() }
                    }
                }
            }
        }
    }

    override suspend fun PipelineContext<S, I, A>.recover(e: Exception) {
        withContext(this@StoreImpl) {
            onException(e)?.let { throw UnhandledStoreException(it) }
        }
    }

    override fun CoroutineScope.subscribe(
        block: suspend Provider<S, I, A>.() -> Unit
    ): Job = launch {
        newSubscriber()
        block(this@StoreImpl)
        if (config.debuggable) throw NonSuspendingSubscriberException()
    }.apply {
        invokeOnCompletion { removeSubscriber() }
    }

    override fun close() {
        // completion handler will cleanup the pipeline later
        launchJob.value?.cancel()
    }

    override fun hashCode() = name?.hashCode() ?: super.hashCode()
    override fun toString(): String = name ?: super.toString()
    override fun equals(other: Any?): Boolean {
        if (other !is Store<*, *, *>) return false
        if (other.name == null && name == null) return other === this
        return name == other.name
    }
}
