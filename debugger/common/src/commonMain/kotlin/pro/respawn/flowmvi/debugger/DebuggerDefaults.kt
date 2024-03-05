package pro.respawn.flowmvi.debugger

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

internal expect val DefaultHost: String

object DebuggerDefaults {

    const val Port = 9684
    const val LocalHost = "localhost"
    val ReconnectionDelay = 20.seconds
    val ClientHost get() = DefaultHost
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
