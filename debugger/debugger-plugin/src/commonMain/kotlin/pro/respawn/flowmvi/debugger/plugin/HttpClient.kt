package pro.respawn.flowmvi.debugger.plugin

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.DataConversion
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.SaveBodyPlugin
import io.ktor.client.plugins.addDefaultResponseValidation
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.compression.ContentEncodingConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pro.respawn.flowmvi.debugger.DebuggerDefaults.DefaultJson

@PublishedApi
internal val DebugHttpClient: HttpClient by lazy { HttpClient(DefaultJson) }

@Suppress("MagicNumber")
internal fun HttpClient(
    json: Json,
    pingInterval: Long = 5000L
) = HttpClient(CIO) {
    install(WebSockets) {
        pingIntervalMillis = pingInterval
        contentConverter = KotlinxWebsocketSerializationConverter(json)
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
        sanitizeHeader { it == HttpHeaders.Authorization }
    }
    install(ContentNegotiation) { json(json) }
    install(HttpRequestRetry) {
        retryOnServerErrors(2)
        constantDelay(1000)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 8000
        connectTimeoutMillis = 8000
    }
    install(DataConversion)
    install(ContentEncoding) {
        mode = ContentEncodingConfig.Mode.All // compress requests
        deflate(1f)
        gzip(0.8f)
        identity(0.5f)
    }
    addDefaultResponseValidation()
    expectSuccess = true
    followRedirects = true
    engine {
        pipelining = true
        endpoint { }
    }
}
