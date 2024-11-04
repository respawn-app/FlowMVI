package pro.respawn.flowmvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.exceptions.NonSuspendingSubscriberException
import pro.respawn.flowmvi.exceptions.SubscribeBeforeStartException
import pro.respawn.flowmvi.exceptions.UnhandledIntentException
import pro.respawn.flowmvi.modules.ActionModule
import pro.respawn.flowmvi.modules.IntentModule
import pro.respawn.flowmvi.modules.RecoverModule
import pro.respawn.flowmvi.modules.RestartableLifecycle
import pro.respawn.flowmvi.modules.StateModule
import pro.respawn.flowmvi.modules.SubscriptionModule
import pro.respawn.flowmvi.modules.actionModule
import pro.respawn.flowmvi.modules.intentModule
import pro.respawn.flowmvi.modules.launchPipeline
import pro.respawn.flowmvi.modules.observeSubscribers
import pro.respawn.flowmvi.modules.recoverModule
import pro.respawn.flowmvi.modules.restartableLifecycle
import pro.respawn.flowmvi.modules.stateModule
import pro.respawn.flowmvi.modules.subscriptionModule

internal class StoreImpl<S : MVIState, I : MVIIntent, A : MVIAction>(
    private val config: StoreConfiguration<S>,
    plugin: StorePlugin<S, I, A>,
    recover: RecoverModule<S, I, A> = recoverModule(plugin),
    subs: SubscriptionModule = subscriptionModule(),
    states: StateModule<S> = stateModule(config.initial, config.atomicStateUpdates),
    intents: IntentModule<I> = intentModule(config.parallelIntents, config.intentCapacity, config.onOverflow),
    actions: ActionModule<A> = actionModule(config.actionShareBehavior),
) : Store<S, I, A>,
    Provider<S, I, A>,
    RestartableLifecycle by restartableLifecycle(),
    StorePlugin<S, I, A> by plugin,
    RecoverModule<S, I, A> by recover,
    SubscriptionModule by subs,
    StateModule<S> by states,
    IntentModule<I> by intents,
    ActionModule<A> by actions {

    override val name by config::name

    override fun start(scope: CoroutineScope) = launchPipeline(
        parent = scope,
        config = config,
        onAction = { action -> onAction(action)?.let { this@StoreImpl.action(it) } },
        onTransformState = { transform ->
            this@StoreImpl.updateState { onState(this, transform()) ?: this }
        },
        onStop = {
            close() // makes sure to also clear the reference from RestartableLifecycle
            onStop(it)
        },
        onStart = { lifecycle ->
            beginStartup(lifecycle)
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
                    lifecycle.completeStartup()
                }
            }
        }
    )

    override fun CoroutineScope.subscribe(
        block: suspend Provider<S, I, A>.() -> Unit
    ): Job = launch {
        if (!isActive && !config.allowIdleSubscriptions) throw SubscribeBeforeStartException()
        launch { awaitUnsubscription() }
        block(this@StoreImpl)
        if (config.debuggable) throw NonSuspendingSubscriberException()
        cancel()
    }

    override fun hashCode() = name?.hashCode() ?: super.hashCode()
    override fun toString(): String = name ?: super.toString()
    override fun equals(other: Any?): Boolean {
        if (other !is Store<*, *, *>) return false
        if (other.name == null && name == null) return other === this
        return name == other.name
    }
}
