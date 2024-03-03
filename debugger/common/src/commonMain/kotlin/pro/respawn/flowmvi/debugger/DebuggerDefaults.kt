package pro.respawn.flowmvi.debugger

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

object DebuggerDefaults {

    const val Port = 6789
    const val LocalHost = "127.0.0.1"
    const val EmulatorHost = "127.0.0.2"
    val ReconnectionDelay = 20.seconds
    const val DefaultHistorySize: Int = 100

    @OptIn(ExperimentalSerializationApi::class)
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
