package pro.respawn.flowmvi.debugger

import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

object DebuggerDefaults {

    const val Port = 6789
    const val Host = "127.0.0.1"
    val ReconnectionDelay = 20.seconds
    const val DefaultHistorySize: Int = 100

    val DefaultJson by lazy {
        Json {
            prettyPrint = true
            allowTrailingComma = true
            coerceInputValues = true
            decodeEnumsCaseInsensitive = true
            explicitNulls = false
            ignoreUnknownKeys = true
        }
    }
}
