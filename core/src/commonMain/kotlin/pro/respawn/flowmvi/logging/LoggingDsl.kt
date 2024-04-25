package pro.respawn.flowmvi.logging

import pro.respawn.flowmvi.api.PipelineContext

/**
 * Alias for [StoreLogger.log] with optional parameters
 */
public operator fun StoreLogger.invoke(
    level: StoreLogLevel,
    tag: String? = null,
    message: () -> String,
): Unit = log(level, tag, message)

/**
 * Alias for [StoreLogger.log] that can log exceptions
 */
public operator fun StoreLogger.invoke(
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

/**
 * Write a message to [StoreLogger]. Tag is the [Store.name] by default.
 */
public fun PipelineContext<*, *, *>.log(
    level: StoreLogLevel = StoreLogLevel.Debug,
    tag: String? = config.name,
    message: () -> String
): Unit = config.logger(level, tag, message)

/**
 * Write a message to [StoreLogger]. Tag is the [Store.name] by default.
 */
public fun PipelineContext<*, *, *>.log(
    e: Exception,
    level: StoreLogLevel = StoreLogLevel.Error,
    tag: String? = config.name
): Unit = config.logger(e, level, tag)

internal inline fun template(
    level: StoreLogLevel,
    tag: String?,
    message: () -> String
) = "${level.asSymbol} ${if (tag.isNullOrBlank()) "" else "$tag: "}${message()}"
