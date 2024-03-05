package pro.respawn.flowmvi.logging

/**
 * Alias for [StoreLogger.log] with optional parameters
 */
public operator fun StoreLogger?.invoke(
    level: StoreLogLevel,
    tag: String? = null,
    message: () -> String,
) {
    if (this == null) return
    log(level, tag, message)
}

/**
 * Alias for [StoreLogger.log] that can log exceptions
 */
public operator fun StoreLogger?.invoke(
    level: StoreLogLevel,
    tag: String? = null,
    e: Exception,
): Unit = invoke(level, tag) { e.stackTraceToString() }

internal val StoreLogLevel.asSymbol
    get() = when (this) {
        StoreLogLevel.Trace -> "⚪️"
        StoreLogLevel.Debug -> "🟢"
        StoreLogLevel.Info -> "🔵"
        StoreLogLevel.Warn -> "🟡"
        StoreLogLevel.Error -> "🔴"
    }
