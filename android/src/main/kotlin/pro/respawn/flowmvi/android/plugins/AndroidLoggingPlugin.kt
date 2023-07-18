package pro.respawn.flowmvi.android.plugins

import android.util.Log
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.plugins.LogPriority
import pro.respawn.flowmvi.plugins.loggingPlugin

public fun <S : MVIState, I : MVIIntent, A : MVIAction> androidLoggingPlugin(
    tag: String,
    level: Int? = null,
): StorePlugin<S, I, A> = loggingPlugin(tag) { priority, _, msg ->
    Log.println(level ?: priority.androidPriority, tag, msg)
}

public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.androidLoggingPlugin(
    level: Int? = null,
): StorePlugin<S, I, A> = loggingPlugin(name) { priority, tag, msg ->
    Log.println(level ?: priority.androidPriority, tag, msg)
}

internal val LogPriority.androidPriority
    get() = when (this) {
        LogPriority.Trace -> Log.VERBOSE
        LogPriority.Debug -> Log.DEBUG
        LogPriority.Info -> Log.INFO
        LogPriority.Warn -> Log.WARN
        LogPriority.Error -> Log.ERROR
    }
