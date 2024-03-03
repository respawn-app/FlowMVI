package pro.respawn.flowmvi.debugger.plugin

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.debugger.DebuggerDefaults
import pro.respawn.flowmvi.debugger.DebuggerDefaults.DefaultHistorySize
import pro.respawn.flowmvi.debugger.client.remoteDebugger
import pro.respawn.flowmvi.dsl.StoreBuilder
import kotlin.time.Duration

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> debuggerPlugin(
    storeName: String,
    historySize: Int = DefaultHistorySize,
    host: String = DebuggerDefaults.Host,
    port: Int = DebuggerDefaults.Port,
    reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
): StorePlugin<S, I, A> = pro.respawn.flowmvi.debugger.client.debuggerPlugin(
    storeName = storeName,
    client = DebugHttpClient,
    historySize = historySize,
    host = host,
    port = port,
    reconnectionDelay = reconnectionDelay
)

@FlowMVIDSL
public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.remoteDebugger(
    historySize: Int = DefaultHistorySize,
    host: String = DebuggerDefaults.Host,
    port: Int = DebuggerDefaults.Port,
    reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
): Unit = remoteDebugger(
    client = DebugHttpClient,
    historySize = historySize,
    host = host,
    port = port,
    reconnectionDelay = reconnectionDelay
)
