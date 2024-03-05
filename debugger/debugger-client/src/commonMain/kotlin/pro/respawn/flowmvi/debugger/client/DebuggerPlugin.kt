package pro.respawn.flowmvi.debugger.client

import io.ktor.client.HttpClient
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.debugger.DebuggerDefaults
import pro.respawn.flowmvi.debugger.DebuggerDefaults.DefaultHistorySize
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreAction
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreException
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreIntent
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreStarted
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreStateChanged
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreSubscribed
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreUnsubscribed
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.plugins.TimeTravel
import pro.respawn.flowmvi.plugins.compositePlugin
import pro.respawn.flowmvi.plugins.timeTravelPlugin
import pro.respawn.flowmvi.util.nameByType
import kotlin.time.Duration

@PublishedApi
internal const val NonDebuggableStoreMessage: String = """
Store must be debuggable in order to use the debugger.
Please set `debuggable = true` before installing the plugin.
Don't include debug code in production builds.
Suppress this error by using install(debuggerPlugin) directly.
"""

@Suppress("UNUSED_PARAMETER")
private fun <S : MVIState, I : MVIIntent, A : MVIAction> DebugClientStore.asPlugin(
    storeName: String,
    timeTravel: TimeTravel<S, I, A>, // will be used later
) = plugin<S, I, A> {
    this.name = "${storeName}DebuggerPlugin"
    onStart {
        start(this)
        emit(StoreStarted(storeName))
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
    onStop { close() }
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
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> debuggerPlugin(
    storeName: String,
    client: HttpClient,
    timeTravel: TimeTravel<S, I, A>,
    host: String = DebuggerDefaults.ClientHost,
    port: Int = DebuggerDefaults.Port,
    reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
): StorePlugin<S, I, A> = debugClientStore(
    clientName = storeName,
    client = client,
    host = host,
    port = port,
    reconnectionDelay = reconnectionDelay
).asPlugin(storeName, timeTravel)

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
    storeName: String,
    client: HttpClient,
    historySize: Int = DefaultHistorySize,
    host: String = DebuggerDefaults.ClientHost,
    port: Int = DebuggerDefaults.Port,
    reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
): StorePlugin<S, I, A> {
    val tt = TimeTravel<S, I, A>(maxHistorySize = historySize)
    return compositePlugin(
        name = "${storeName}DebuggerPlugin",
        plugins = setOf(
            timeTravelPlugin(timeTravel = tt, name = "${storeName}DebuggerTimeTravel"),
            debuggerPlugin(
                storeName = storeName,
                client = client,
                timeTravel = tt,
                host = host,
                port = port,
                reconnectionDelay = reconnectionDelay
            )
        ),
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
public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.enableRemoteDebugging(
    client: HttpClient,
    historySize: Int = DefaultHistorySize,
    host: String = DebuggerDefaults.ClientHost,
    port: Int = DebuggerDefaults.Port,
    reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
    name: String = this.name ?: nameByType<S>() ?: "Store",
) {
    require(debuggable) { NonDebuggableStoreMessage }
    install(
        debuggerPlugin(
            storeName = name,
            client = client,
            historySize = historySize,
            host = host,
            port = port,
            reconnectionDelay = reconnectionDelay
        )
    )
}
