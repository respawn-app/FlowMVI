package pro.respawn.flowmvi.debugger.client

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.utils.io.CancellationException
import pro.respawn.flowmvi.debugger.DebuggerDefaults
import pro.respawn.flowmvi.metrics.MetricsSink

/**
 * Creates a [MetricsSink] that sends store metrics to the FlowMVI Debugger server.
 *
 * @param client The [HttpClient] to use for sending metrics.
 * @param host The debugger server host. Defaults to [DebuggerDefaults.ClientHost].
 * @param port The debugger server port. Defaults to [DebuggerDefaults.Port].
 * @param onError A callback invoked when an error occurs while sending metrics. By default, errors are swallowed.
 * @return A [MetricsSink] that forwards metrics to the debugger server.
 */
public fun DebuggerSink(
    client: HttpClient,
    host: String = DebuggerDefaults.ClientHost,
    port: Int = DebuggerDefaults.Port,
    onError: (e: Exception) -> Unit = { /* swallow */ },
): MetricsSink = MetricsSink { snapshot ->
    val storeId = snapshot.meta.storeId ?: return@MetricsSink
    runCatching {
        client.post {
            url.host = host
            url.port = port
            contentType(ContentType.Application.Json)
            url.path(storeId, "metrics")
            setBody(snapshot)
        }
    }.onFailure {
        when (it) {
            !is Exception, is CancellationException -> throw it
            else -> onError(it)
        }
    }
}
