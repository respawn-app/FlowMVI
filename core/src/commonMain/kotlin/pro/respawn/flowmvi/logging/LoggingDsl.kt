package pro.respawn.flowmvi.logging

import pro.respawn.flowmvi.api.PipelineContext

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
    e: Exception,
    level: StoreLogLevel = StoreLogLevel.Error,
    tag: String? = null,
): Unit = invoke(level, tag) { e.stackTraceToString() }

/**
 * Returns an emoji resembling this log level.
 */
public val StoreLogLevel.asSymbol: String
    get() = when (this) {
        StoreLogLevel.Trace -> "⚪️"
        StoreLogLevel.Debug -> "🟢"
        StoreLogLevel.Info -> "🔵"
        StoreLogLevel.Warn -> "🟡"
        StoreLogLevel.Error -> "🔴"
    }

public fun PipelineContext<*, *, *>.log(level: StoreLogLevel, message: () -> String) {
    config.logger(level, config.name, message)
}

public fun PipelineContext<*, *, *>.log(e: Exception, level: StoreLogLevel = StoreLogLevel.Error) {
    config.logger(e, level, config.name)
}
