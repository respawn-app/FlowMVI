package pro.respawn.flowmvi.debugger.plugin

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.LazyPlugin
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.debugger.DebuggerDefaults
import pro.respawn.flowmvi.debugger.client.debuggerPlugin
import pro.respawn.flowmvi.debugger.client.enableRemoteDebugging
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.plugins.TimeTravel
import kotlin.time.Duration

/**
 * Creates a new remote debugging plugin.
 * * This plugin will use the default HttpClient. If you want to use a custom client, depend on debugger-client
 *   module instead.
 * * This overload will create and install a [TimeTravel] plugin for you.
 *
 * This plugin **must not be used** in production code.
 * Better yet, do not include the debugger-client dependency at all in production builds,
 * because the plugin depends on a lot of things you may not need for your application.
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> debuggerPlugin(
    historySize: Int = DebuggerDefaults.DefaultHistorySize,
    host: String = DebuggerDefaults.ClientHost,
    port: Int = DebuggerDefaults.Port,
    reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
): LazyPlugin<S, I, A> = debuggerPlugin(
    client = DebugHttpClient,
    historySize = historySize,
    host = host,
    port = port,
    reconnectionDelay = reconnectionDelay
)

/**
 * Create and install a new remote debugging plugin.
 * * This plugin will use the default HttpClient. If you want to use a custom client, depend on debugger-client
 *   module instead.
 * * This overload will create and install a [TimeTravel] plugin for you.
 * * This overload will throw if the store is **not** debuggable for safety reasons. If you still want to override
 *   this behavior (although strictly not recommended), please use another overload like [debuggerPlugin].
 *
 * This plugin **must not be used** in production code.
 * Better yet, do not include the debugger-client dependency at all in production builds,
 * because the plugin depends on a lot of things you may not need for your application.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.enableRemoteDebugging(
    historySize: Int = DebuggerDefaults.DefaultHistorySize,
    host: String = DebuggerDefaults.ClientHost,
    port: Int = DebuggerDefaults.Port,
    reconnectionDelay: Duration = DebuggerDefaults.ReconnectionDelay,
): Unit = enableRemoteDebugging(
    client = DebugHttpClient,
    historySize = historySize,
    host = host,
    port = port,
    reconnectionDelay = reconnectionDelay
)
