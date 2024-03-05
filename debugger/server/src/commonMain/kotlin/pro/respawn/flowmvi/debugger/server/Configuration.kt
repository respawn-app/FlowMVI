package pro.respawn.flowmvi.debugger.server

import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.CompressionConfig
import io.ktor.server.plugins.compression.deflate
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.compression.identity
import io.ktor.server.plugins.compression.matchContentType
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.dataconversion.DataConversion
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.partialcontent.PartialContent
import io.ktor.server.websocket.WebSockets
import org.slf4j.event.Level
import pro.respawn.flowmvi.debugger.DebuggerDefaults

internal fun Application.configureDebugServer() {
    install(CORS) { anyHost() }
    install(ContentNegotiation) {
        json(DebuggerDefaults.DefaultJson)
        checkAcceptHeaderCompliance = false
    }
    install(DefaultHeaders)
    install(DataConversion)
    install(PartialContent)
    install(WebSockets)
    install(CallLogging) {
        level = if (this@configureDebugServer.developmentMode) Level.DEBUG else Level.INFO
    }
    install(Compression) {
        mode = CompressionConfig.Mode.All
        matchContentType(ContentType.Application.Json)
        deflate { priority = 1.0 }
        gzip { priority = 0.8 }
        identity { priority = 0.5 }
    }
}
