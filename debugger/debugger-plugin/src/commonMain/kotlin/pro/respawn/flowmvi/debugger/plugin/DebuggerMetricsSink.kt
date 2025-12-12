package pro.respawn.flowmvi.debugger.plugin

import pro.respawn.flowmvi.debugger.DebuggerDefaults
import pro.respawn.flowmvi.debugger.client.DebuggerSink
import pro.respawn.flowmvi.metrics.MetricsSink

public fun DebuggerSink(
    host: String = DebuggerDefaults.ClientHost,
    port: Int = DebuggerDefaults.Port,
    onError: (e: Exception) -> Unit = { /* swallow */ },
): MetricsSink = DebuggerSink(DebugHttpClient, host, port, onError)
