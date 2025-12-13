package pro.respawn.flowmvi.debugger.plugin

import pro.respawn.flowmvi.debugger.DebuggerDefaults
import pro.respawn.flowmvi.debugger.client.DebuggerSink
import pro.respawn.flowmvi.metrics.MetricsSink

/**
 * Creates a [MetricsSink] that sends store metrics to the FlowMVI Debugger server.
 *
 * This overload uses a default [HttpClient][io.ktor.client.HttpClient] configured for the debugger.
 *
 * @param host The debugger server host. Defaults to [DebuggerDefaults.ClientHost].
 * @param port The debugger server port. Defaults to [DebuggerDefaults.Port].
 * @param onError A callback invoked when an error occurs while sending metrics. By default, errors are swallowed.
 * @return A [MetricsSink] that forwards metrics to the debugger server.
 */
public fun DebuggerSink(
    host: String = DebuggerDefaults.ClientHost,
    port: Int = DebuggerDefaults.Port,
    onError: (e: Exception) -> Unit = { /* swallow */ },
): MetricsSink = DebuggerSink(DebugHttpClient, host, port, onError)
