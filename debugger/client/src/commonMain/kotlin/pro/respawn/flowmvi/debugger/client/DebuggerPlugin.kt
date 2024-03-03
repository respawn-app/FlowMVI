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

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> debuggerPlugin(
    storeName: String,
    client: HttpClient,
    timeTravel: TimeTravel<S, I, A>,
    host: String = DebuggerDefaults.LocalHost,
    port: Int = DebuggerDefaults.Port,
    reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
): StorePlugin<S, I, A> = plugin {
    this.name = "${storeName}DebuggerPlugin"
    with(DebugClient(storeName, client, timeTravel, host, port, reconnectionDelay)) {
        onStart {
            launch(this)
            send(StoreStarted(storeName))
        }
        onIntent {
            send(StoreIntent(it))
            it
        }
        onAction {
            send(StoreAction(it))
            it
        }
        onState { old, new ->
            send(StoreStateChanged(old, new))
            new
        }
        onException {
            send(StoreException(it))
            it
        }
        onSubscribe {
            send(StoreSubscribed(it))
        }
        onUnsubscribe {
            send(StoreUnsubscribed(it))
        }
        onStop {
            // nothing to do at this point - the debugger will know that store has disconnected
        }
    }
}

public fun <S : MVIState, I : MVIIntent, A : MVIAction> debuggerPlugin(
    storeName: String,
    client: HttpClient,
    historySize: Int = DefaultHistorySize,
    host: String = DebuggerDefaults.LocalHost,
    port: Int = DebuggerDefaults.Port,
    reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
): StorePlugin<S, I, A> {
    val tt = TimeTravel<S, I, A>(maxHistorySize = historySize)
    return compositePlugin(
        setOf(
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

@FlowMVIDSL
public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.remoteDebugger(
    client: HttpClient,
    historySize: Int = DefaultHistorySize,
    host: String = DebuggerDefaults.LocalHost,
    port: Int = DebuggerDefaults.Port,
    reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
) {
    require(debuggable) { NonDebuggableStoreMessage }
    install(
        debuggerPlugin(
            storeName = name ?: nameByType<S>() ?: "Store",
            client = client,
            historySize = historySize,
            host = host,
            port = port,
            reconnectionDelay = reconnectionDelay
        )
    )
}
