@file:OptIn(InternalFlowMVIAPI::class)

package pro.respawn.flowmvi.debugger.client

import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.LazyPlugin
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.debugger.DebuggerDefaults
import pro.respawn.flowmvi.debugger.DebuggerDefaults.DefaultHistorySize
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreAction
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreException
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreIntent
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreStarted
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreStateChanged
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreStopped
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreSubscribed
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreUnsubscribed
import pro.respawn.flowmvi.debugger.model.ServerEvent
import pro.respawn.flowmvi.debugger.model.ServerEvent.ResendLastAction
import pro.respawn.flowmvi.debugger.model.ServerEvent.ResendLastIntent
import pro.respawn.flowmvi.debugger.model.ServerEvent.RethrowLastException
import pro.respawn.flowmvi.debugger.model.ServerEvent.RollbackState
import pro.respawn.flowmvi.debugger.model.ServerEvent.RollbackToInitialState
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.dsl.updateStateImmediate
import pro.respawn.flowmvi.logging.warn
import pro.respawn.flowmvi.plugins.NoOpPlugin
import pro.respawn.flowmvi.plugins.TimeTravel
import pro.respawn.flowmvi.plugins.compositePlugin
import pro.respawn.flowmvi.plugins.timeTravelPlugin
import kotlin.time.Duration

@PublishedApi
internal const val NonDebuggableStoreMessage: String = """
The debugger has been disabled because store is not debuggable!
Please set `debuggable = true` before installing the plugin.
Don't include debug code in production builds.
"""

private fun <S : MVIState, I : MVIIntent, A : MVIAction> DebugClientStore.asPlugin(
    clientKey: String?,
    timeTravel: TimeTravel<S, I, A>, // will be used later
) = plugin<S, I, A> {
    this.name = "${clientKey.orEmpty()}Debugger"
    onStart ctx@{
        start(this)
        subscribe { // subscribe to store events
            actions.collectLatest { event ->
                when (event) {
                    is ResendLastAction -> timeTravel.actions.lastOrNull()?.let { action(it) }
                    is ResendLastIntent -> timeTravel.intents.lastOrNull()?.let { intent(it) }
                    is RethrowLastException -> timeTravel.exceptions.lastOrNull()?.let {
                        // throw it async to let the exception handler handle it
                        val _ = runCatching { this@ctx.launch { throw it } }
                    }
                    is RollbackState -> timeTravel.states.getOrNull(
                        timeTravel.states.lastIndex - 1
                        // ignore plugins, including self, to not loop the event
                    )?.let { previous -> updateStateImmediate { previous } }
                    is RollbackToInitialState -> updateStateImmediate { this@ctx.config.initial }
                    is ServerEvent.Stop -> this@ctx.close()
                }
            }
        }
        emit(StoreStarted(clientKey))
    }
    onIntent {
        emit(StoreIntent(it))
        it
    }
    onAction {
        emit(StoreAction(it))
        it
    }
    onState { old, new ->
        emit(StoreStateChanged(old, new))
        new
    }
    onException {
        emit(StoreException(it))
        it
    }
    onSubscribe { emit(StoreSubscribed(it)) }
    onUnsubscribe { emit(StoreUnsubscribed(it)) }
    onStop {
        intent(StoreStopped(config.name, config.id))
        close()
    }
}

/**
 * Creates a new remote debugging plugin.
 * * You must also provide a [TimeTravel] for the plugin to use where it will track events.
 * * This overload uses a custom [client] for networking. This client **must** be configured
 *   to correctly serialize and use websockets for connection.
 *   See the documentation to learn how to set up the client.
 *   If you want to use the default client, depend on debugger-plugin
 *   module instead.
 *
 * This plugin **must not be used** in production code.
 * Better yet, do not include the debugger-client dependency at all in production builds,
 * because the plugin depends on a lot of things you may not need for your application.
 */
@OptIn(InternalFlowMVIAPI::class)
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> debuggerPlugin(
    client: HttpClient,
    timeTravel: TimeTravel<S, I, A>,
    host: String = DebuggerDefaults.ClientHost,
    port: Int = DebuggerDefaults.Port,
    reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
): LazyPlugin<S, I, A> = LazyPlugin { config ->
    config.ensureDebuggable { return@LazyPlugin NoOpPlugin() }
    debugClientStore(
        clientKey = config.name,
        clientId = config.id,
        client = client,
        host = host,
        port = port,
        reconnectionDelay = reconnectionDelay
    ).asPlugin(config.name, timeTravel)
}

/**
 * Creates a new remote debugging plugin.
 * * This overload will create and install a [TimeTravel] plugin for you.
 * * This overload uses a custom [client] for networking. This client **must** be configured
 *   to correctly serialize and use websockets for connection.
 *   See the documentation to learn how to set up the client.
 *   If you want to use the default client, depend on debugger-plugin
 *   module instead.
 *
 * This plugin **must not be used** in production code.
 * Better yet, do not include the debugger-client dependency at all in production builds,
 * because the plugin depends on a lot of things you may not need for your application.
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> debuggerPlugin(
    client: HttpClient,
    historySize: Int = DefaultHistorySize,
    host: String = DebuggerDefaults.ClientHost,
    port: Int = DebuggerDefaults.Port,
    reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
): LazyPlugin<S, I, A> = LazyPlugin { config ->
    config.ensureDebuggable { return@LazyPlugin NoOpPlugin() }
    val tt = TimeTravel<S, I, A>(maxHistorySize = historySize)
    compositePlugin(
        name = "${config.name}DebuggerPlugin",
        plugins = listOf(
            timeTravelPlugin(timeTravel = tt, name = "${config.name}DebuggerTimeTravel"),
            debuggerPlugin(
                client = client,
                timeTravel = tt,
                host = host,
                port = port,
                reconnectionDelay = reconnectionDelay
            )
        ).map { it.invoke(config) },
    )
}

/**
 * Create and install a new remote debugging plugin.
 * * This overload will create and install a [TimeTravel] plugin for you.
 * * This overload uses a custom [client] for networking. This client **must** be configured
 *   to correctly serialize and use websockets for connection.
 *   See the documentation to learn how to set up the client.
 *   If you want to use the default client, depend on debugger-plugin
 *   module instead.
 * * This overload will throw if the store is **not** debuggable for safety reasons. If you still want to override
 *   this behavior (although strictly not recommended), please use another overload like [debuggerPlugin].
 *
 * This plugin **must not be used** in production code.
 * Better yet, do not include the debugger-client dependency at all in production builds,
 * because the plugin depends on a lot of things you may not need for your application.
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.enableRemoteDebugging(
    client: HttpClient,
    historySize: Int = DefaultHistorySize,
    host: String = DebuggerDefaults.ClientHost,
    port: Int = DebuggerDefaults.Port,
    reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
): Unit = install(
    debuggerPlugin(
        client = client,
        historySize = historySize,
        host = host,
        port = port,
        reconnectionDelay = reconnectionDelay
    )
)

private inline fun StoreConfiguration<*>.ensureDebuggable(orElse: () -> Unit) {
    if (!debuggable) {
        logger.warn(name) { NonDebuggableStoreMessage }
        orElse()
    }
}
