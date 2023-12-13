package pro.respawn.flowmvi.plugins

import android.util.Log
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder

private const val MaxLogcatMessageLength = 4000
private const val MinLogcatMessageLength = 3000

/**
 * Creates a [loggingPlugin] that is suitable for each targeted platform.
 * This plugin will log to:
 * * Logcat on Android,
 * * NSLog on apple targets,
 * * console.log on JS,
 * * stdout on mingw/native
 * * System.out on JVM.
 */
@FlowMVIDSL
public actual fun <S : MVIState, I : MVIIntent, A : MVIAction> platformLoggingPlugin(
    tag: String?,
    level: StoreLogLevel?
): StorePlugin<S, I, A> = androidLoggingPlugin(tag, level?.asLogPriority)

/**
 * Create a new [loggingPlugin] that prints using android's [Log].
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> androidLoggingPlugin(
    tag: String? = null,
    level: Int? = null,
): StorePlugin<S, I, A> = loggingPlugin(tag) { priority, _, msg ->
    chunkedLog(msg) { Log.println(level ?: priority.asLogPriority, tag, it) }
}

/**
 * Create a new [loggingPlugin] that prints using android's [Log].
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.androidLoggingPlugin(
    name: String? = this.name,
    level: Int? = null,
): StorePlugin<S, I, A> = loggingPlugin(name) { priority, tag, msg ->
    Log.println(level ?: priority.asLogPriority, tag, msg)
}

internal val StoreLogLevel.asLogPriority
    get() = when (this) {
        StoreLogLevel.Trace -> Log.VERBOSE
        StoreLogLevel.Debug -> Log.DEBUG
        StoreLogLevel.Info -> Log.INFO
        StoreLogLevel.Warn -> Log.WARN
        StoreLogLevel.Error -> Log.ERROR
    }

/**
 * Credits to Ktor for the implementation
 */
private tailrec fun chunkedLog(
    message: String,
    maxLength: Int = MaxLogcatMessageLength,
    minLength: Int = MinLogcatMessageLength,
    delegate: (String) -> Unit
) {
    // String to be logged is longer than the max...
    if (message.length > maxLength) {
        var msgSubstring = message.substring(0, maxLength)
        var msgSubstringEndIndex = maxLength

        // Try to find a substring break at a newline char.
        msgSubstring.lastIndexOf('\n').let { lastIndex ->
            if (lastIndex >= minLength) {
                msgSubstring = msgSubstring.substring(0, lastIndex)
                // skip over new line char
                msgSubstringEndIndex = lastIndex + 1
            }
        }

        // Log the substring.
        delegate(msgSubstring)

        // Recursively log the remainder.
        chunkedLog(message.substring(msgSubstringEndIndex), maxLength, minLength, delegate)
    } else {
        delegate(message)
    } // String to be logged is shorter than the max...
}
