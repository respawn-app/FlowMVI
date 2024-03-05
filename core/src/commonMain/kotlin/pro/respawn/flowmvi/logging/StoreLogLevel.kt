package pro.respawn.flowmvi.logging

/**
 * Log level of this store. Override using [logging] or leave as a default for sensible log levels.
 * Not used when the logger does not support levels, such as with [consoleLoggingPlugin].
 */
public enum class StoreLogLevel {

    Trace, Debug, Info, Warn, Error
}
