package pro.respawn.flowmvi.logging

import android.util.Log

/**
 * Convert [StoreLogLevel] to Android [Log] priority.
 */
public val StoreLogLevel.asLogPriority: Int
    get() = when (this) {
        StoreLogLevel.Trace -> Log.VERBOSE
        StoreLogLevel.Debug -> Log.DEBUG
        StoreLogLevel.Info -> Log.INFO
        StoreLogLevel.Warn -> Log.WARN
        StoreLogLevel.Error -> Log.ERROR
    }

internal val Int.asStoreLogLevel
    get() = when (this) {
        Log.VERBOSE -> StoreLogLevel.Trace
        Log.DEBUG -> StoreLogLevel.Debug
        Log.WARN -> StoreLogLevel.Warn
        Log.INFO -> StoreLogLevel.Info
        Log.ASSERT, Log.ERROR -> StoreLogLevel.Error
        else -> error("Not an android Log level")
    }
