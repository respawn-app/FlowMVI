package pro.respawn.flowmvi.android.plugins

import android.util.Log
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.plugins.StoreLogLevel
import pro.respawn.flowmvi.plugins.loggingPlugin

/**
 * Create a new [loggingPlugin] that prints using android's [Log].
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> androidLoggingPlugin(
    tag: String? = null,
    level: Int? = null,
): StorePlugin<S, I, A> = loggingPlugin(tag) { priority, _, msg ->
    Log.println(level ?: priority.asLogPriority, tag, msg)
}

/**
 * Create a new [loggingPlugin] that prints using android's [Log].
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.androidLoggingPlugin(
    tag: String? = name,
    level: Int? = null,
): StorePlugin<S, I, A> = loggingPlugin(tag) { priority, tag, msg ->
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
