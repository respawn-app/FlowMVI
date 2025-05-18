@file:OptIn(InternalFlowMVIAPI::class)

package pro.respawn.flowmvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.annotation.NotIntendedForInheritance
import pro.respawn.flowmvi.api.ActionProvider
import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.ImmediateStateReceiver
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.api.StateProvider
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.api.context.ShutdownContext
import pro.respawn.flowmvi.exceptions.NonSuspendingSubscriberException
import pro.respawn.flowmvi.exceptions.SubscribeBeforeStartException
import pro.respawn.flowmvi.impl.plugin.PluginInstance
import pro.respawn.flowmvi.modules.RecoverModule
import pro.respawn.flowmvi.modules.RestartableLifecycle
import pro.respawn.flowmvi.modules.StateModule
import pro.respawn.flowmvi.modules.SubscriptionModule
import pro.respawn.flowmvi.modules.actionModule
import pro.respawn.flowmvi.modules.catch
import pro.respawn.flowmvi.modules.intentModule
import pro.respawn.flowmvi.modules.launchPipeline
import pro.respawn.flowmvi.modules.observeSubscribers
import pro.respawn.flowmvi.modules.observesSubscribers
import pro.respawn.flowmvi.modules.restartableLifecycle
import pro.respawn.flowmvi.modules.subscriptionModule

@OptIn(NotIntendedForInheritance::class, DelicateStoreApi::class)
internal class StoreImpl<S : MVIState, I : MVIIntent, A : MVIAction>(
    override val config: StoreConfiguration<S>,
    private val plugin: PluginInstance<S, I, A>,
    private val recover: RecoverModule<S, I, A> = RecoverModule(plugin.onException),
    private val stateModule: StateModule<S, I, A> = StateModule(
        config.initial,
        config.stateStrategy,
        config.debuggable,
        plugin.onState,
    ),
) : Store<S, I, A>,
    Provider<S, I, A>,
    ShutdownContext<S, I, A>,
    IntentReceiver<I>,
    ActionProvider<A>,
    ActionReceiver<A>,
    RestartableLifecycle by restartableLifecycle(),
    SubscriptionModule by subscriptionModule(),
    StorePlugin<S, I, A> by plugin,
    StateProvider<S> by stateModule,
    ImmediateStateReceiver<S> by stateModule {

    private val intents = intentModule<S, I, A>(
        config = config,
        onIntent = plugin.onIntent?.let { onIntent -> { intent -> catch(recover) { onIntent(this, intent) } } },
        onUndeliveredIntent = plugin.onUndeliveredIntent?.let { { intent -> it(this, intent) } },
    )

    private val _actions = actionModule<A>(
        behavior = config.actionShareBehavior,
        onUndeliveredAction = plugin.onUndeliveredAction?.let { { action -> it(this, action) } }
    )

    // region pipeline
    override fun start(scope: CoroutineScope) = launchPipeline(
        parent = scope,
        storeConfig = config,
        states = stateModule,
        recover = recover,
        onAction = { action -> onAction(action)?.let { _actions.action(it) } },
        onStop = { e -> close().also { plugin.onStop?.invoke(this, e) } },
        onStart = pipeline@{ lifecycle ->
            beginStartup(lifecycle, config)
            launch {
                catch(recover) { onStart() }
                if (plugin.observesSubscribers) launch {
                    observeSubscribers(
                        onSubscribe = { catch(recover) { onSubscribe(it) } },
                        onUnsubscribe = { catch(recover) { onUnsubscribe(it) } }
                    )
                }
                lifecycle.completeStartup()
                intents.run { reduceForever() }
            }
        }
    )
    // endregion

    override fun CoroutineScope.subscribe(
        block: suspend Provider<S, I, A>.() -> Unit
    ): Job = launch {
        if (!isActive && !config.allowIdleSubscriptions) throw SubscribeBeforeStartException(config.name)
        launch { awaitUnsubscription() }
        block(this@StoreImpl)
        if (!config.allowTransientSubscriptions) throw NonSuspendingSubscriberException(config.name)
        cancel()
    }

    // region contract
    override val name by config::name
    override val states by stateModule::states
    override val actions: Flow<A> by _actions::actions
    override fun send(action: A) = _actions.send(action)
    override suspend fun emit(intent: I) = intents.emit(intent)
    override fun intent(intent: I) = intents.intent(intent)
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
