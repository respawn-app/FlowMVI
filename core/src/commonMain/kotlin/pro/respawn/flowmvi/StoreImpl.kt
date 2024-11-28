package pro.respawn.flowmvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.annotation.NotIntendedForInheritance
import pro.respawn.flowmvi.api.ActionProvider
import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.api.context.ShutdownContext
import pro.respawn.flowmvi.exceptions.NonSuspendingSubscriberException
import pro.respawn.flowmvi.exceptions.SubscribeBeforeStartException
import pro.respawn.flowmvi.exceptions.UnhandledIntentException
import pro.respawn.flowmvi.impl.plugin.PluginInstance
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

@OptIn(NotIntendedForInheritance::class)
internal class StoreImpl<S : MVIState, I : MVIIntent, A : MVIAction>(
    override val config: StoreConfiguration<S>,
    private val plugin: PluginInstance<S, I, A>,
    recover: RecoverModule<S, I, A> = recoverModule(plugin),
    subs: SubscriptionModule = subscriptionModule(),
    states: StateModule<S> = stateModule(config.initial, config.atomicStateUpdates),
) : Store<S, I, A>,
    Provider<S, I, A>,
    ShutdownContext<S, I, A>,
    IntentReceiver<I>,
    ActionProvider<A>,
    ActionReceiver<A>,
    RestartableLifecycle by restartableLifecycle(),
    StorePlugin<S, I, A> by plugin,
    RecoverModule<S, I, A> by recover,
    SubscriptionModule by subs,
    StateModule<S> by states {

    private val intents = intentModule<I>(
        parallel = config.parallelIntents,
        capacity = config.intentCapacity,
        overflow = config.onOverflow,
        onUndeliveredIntent = plugin.onUndeliveredIntent?.let { { intent -> it(this, intent) } },
    )

    private val _actions = actionModule<A>(
        behavior = config.actionShareBehavior,
        onUndeliveredAction = plugin.onUndeliveredAction?.let { { action -> it(this, action) } }
    )

    override suspend fun emit(intent: I) = intents.emit(intent)
    override fun intent(intent: I) = intents.intent(intent)

    // region pipeline
    override fun start(scope: CoroutineScope) = launchPipeline(
        parent = scope,
        storeConfig = config,
        onAction = { action -> onAction(action)?.let { _actions.action(it) } },
        onTransformState = { transform -> this@StoreImpl.updateState { onState(this, transform()) ?: this } },
        onStop = { e -> close().also { plugin.onStop?.invoke(this, e) } },
        onStart = pipeline@{ lifecycle ->
            beginStartup(lifecycle)
            val startup = launch {
                if (plugin.onStart != null) catch { onStart() }
                lifecycle.completeStartup()
            }
            if (plugin.onSubscribe != null || plugin.onUnsubscribe != null) launch {
                startup.join()
                // catch exceptions to not let this job fail
                observeSubscribers(
                    onSubscribe = { catch { onSubscribe(it) } },
                    onUnsubscribe = { catch { onUnsubscribe(it) } }
                )
            }
            if (plugin.onIntent != null) launch {
                startup.join()
                intents.awaitIntents {
                    catch {
                        val result = onIntent(it)
                        if (result != null && config.debuggable) throw UnhandledIntentException(result)
                    }
                }
            }
        }
    )
    // endregion

    override fun CoroutineScope.subscribe(
        block: suspend Provider<S, I, A>.() -> Unit
    ): Job = launch {
        if (!isActive && !config.allowIdleSubscriptions) throw SubscribeBeforeStartException()
        launch { awaitUnsubscription() }
        block(this@StoreImpl)
        if (config.debuggable) throw NonSuspendingSubscriberException()
        cancel()
    }

    // region contract
    override val name by config::name
    override val actions: Flow<A> by _actions::actions

    @DelicateStoreApi
    override fun send(action: A) = _actions.send(action)
    override suspend fun action(action: A) = _actions.action(action)
    override fun hashCode() = name?.hashCode() ?: super.hashCode()
    override fun toString(): String = name ?: super.toString()
    override fun equals(other: Any?): Boolean {
        if (other !is Store<*, *, *>) return false
        if (other.name == null && name == null) return other === this
        return name == other.name
    }
    // endregion
}
