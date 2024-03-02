package pro.respawn.flowmvi.debugger.plugin

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.debugger.core.DefaultHistorySize
import pro.respawn.flowmvi.debugger.core.debuggerPlugin
import pro.respawn.flowmvi.debugger.core.remoteDebugger
import pro.respawn.flowmvi.dsl.StoreBuilder

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> debuggerPlugin(
    storeName: String,
    historySize: Int = DefaultHistorySize,
): StorePlugin<S, I, A> = debuggerPlugin(storeName, DebugHttpClient, historySize)

@FlowMVIDSL
public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.remoteDebugger(
    historySize: Int = DefaultHistorySize,
): Unit = remoteDebugger(client = DebugHttpClient, historySize)
