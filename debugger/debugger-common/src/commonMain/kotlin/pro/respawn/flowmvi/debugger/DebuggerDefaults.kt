package pro.respawn.flowmvi.debugger

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal expect val DefaultHost: String

/**
 * Defaults used for configuring remote debugging server and client.
 */
public object DebuggerDefaults {

    /**
     * Default port
     */
    public const val Port: Int = 9684

    /**
     * Local host, the default usually
     */
    public const val LocalHost: String = "127.0.0.1"

    /**
     * Default delay the stores will use to reconnect to the server
     */
    public val ReconnectionDelay: Duration = 10.seconds

    /**
     * Default client host that the stores will use.
     * It's an emulator host on Android and [LocalHost] on other platforms.
     */
    public val ClientHost: String get() = DefaultHost

    /**
     * Default history size to keep in the server
     */
    public const val DefaultHistorySize: Int = 100

    /**
     * The default Json instance used for serializing debugger responses and requests.
     * Can be overridden if you provide a custom HttpClient
     */
    @OptIn(ExperimentalSerializationApi::class)
    public val DefaultJson: Json by lazy {
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
