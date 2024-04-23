package pro.respawn.flowmvi.store

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.exceptions.NonSuspendingSubscriberException
import pro.respawn.flowmvi.exceptions.UnhandledIntentException
import pro.respawn.flowmvi.modules.ActionModule
import pro.respawn.flowmvi.modules.IntentModule
import pro.respawn.flowmvi.modules.RecoverModule
import pro.respawn.flowmvi.modules.StateModule
import pro.respawn.flowmvi.modules.SubscriptionModule
import pro.respawn.flowmvi.modules.actionModule
import pro.respawn.flowmvi.modules.intentModule
import pro.respawn.flowmvi.modules.launchPipeline
import pro.respawn.flowmvi.modules.observeSubscribers
import pro.respawn.flowmvi.modules.recoverModule
import pro.respawn.flowmvi.modules.stateModule
import pro.respawn.flowmvi.modules.subscriptionModule
import pro.respawn.flowmvi.plugins.compositePlugin

internal class StoreImpl<S : MVIState, I : MVIIntent, A : MVIAction> private constructor(
    private val config: StoreConfiguration<S>,
    plugins: Set<StorePlugin<S, I, A>>,
    plugin: StorePlugin<S, I, A> = compositePlugin(plugins),
    recover: RecoverModule<S, I, A> = recoverModule(plugin),
    subs: SubscriptionModule = subscriptionModule(),
    states: StateModule<S> = stateModule(config.initial, config.atomicStateUpdates),
    intents: IntentModule<I> = intentModule(config.parallelIntents, config.intentCapacity, config.onOverflow),
    actions: ActionModule<A> = actionModule(config.actionShareBehavior),
) : Store<S, I, A>,
    Provider<S, I, A>,
    StorePlugin<S, I, A> by plugin,
    RecoverModule<S, I, A> by recover,
    SubscriptionModule by subs,
    StateModule<S> by states,
    IntentModule<I> by intents,
    ActionModule<A> by actions {

    constructor(
        configuration: StoreConfiguration<S>,
        plugins: Set<StorePlugin<S, I, A>>,
    ) : this(config = configuration, plugins)

    private var launchJob = atomic<Job?>(null)
    override val name by config::name

    override fun start(scope: CoroutineScope): Job = launchPipeline(
        name = name,
        parent = scope + config.coroutineContext,
        onAction = { action -> onAction(action)?.let { this@StoreImpl.action(it) } },
        onTransformState = { transform ->
            this@StoreImpl.updateState { onState(this, transform()) ?: this }
        },
        onStop = {
            checkNotNull(launchJob.getAndSet(null)) { "Store is stopped but was not started before" }
            onStop(it)
        },
        onStart = {
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
    )

    override fun CoroutineScope.subscribe(
        block: suspend Provider<S, I, A>.() -> Unit
    ): Job = subscribe {
        block(this@StoreImpl)
        if (config.debuggable) throw NonSuspendingSubscriberException()
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
