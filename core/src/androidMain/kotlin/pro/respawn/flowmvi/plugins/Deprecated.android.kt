package pro.respawn.flowmvi.plugins

import android.util.Log
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.logging.PlatformStoreLogger
import pro.respawn.flowmvi.logging.StoreLogLevel

private val Int.asStoreLogLevel
    get() = when (this) {
        Log.VERBOSE -> StoreLogLevel.Trace
        Log.DEBUG -> StoreLogLevel.Debug
        Log.WARN -> StoreLogLevel.Warn
        Log.INFO -> StoreLogLevel.Info
        Log.ASSERT, Log.ERROR -> StoreLogLevel.Error
        else -> error("Not an android Log level")
    }

/**
 * Create a new [loggingPlugin] that prints using android's [Log].
 */
@Deprecated(
    "Just use logging plugin with PlatformLogger",
    ReplaceWith("loggingPlugin(PlatformStoreLogger, tag = tag, level =  level)")
)
@Suppress("UnusedParameter") // mistake in the api design
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> androidLoggingPlugin(
    tag: String? = null,
    level: Int? = null,
): StorePlugin<S, I, A> = loggingPlugin(PlatformStoreLogger, tag, level = level?.asStoreLogLevel)

/**
 * Create a new [loggingPlugin] that prints using android's [Log].
 */
@Deprecated(
    "Just use logging plugin with PlatformLogger",
    ReplaceWith("logging(PlatformStoreLogger, name = name, level = level)")
)
@FlowMVIDSL
@Suppress("UnusedParameter") // mistake in the api design
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.androidLoggingPlugin(
    name: String? = this.name,
    level: Int? = null,
): StorePlugin<S, I, A> = loggingPlugin(PlatformStoreLogger, tag = name, level = level?.asStoreLogLevel)
