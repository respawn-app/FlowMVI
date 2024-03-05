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
