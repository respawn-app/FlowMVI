package pro.respawn.flowmvi.logging

/**
 * Log level of this store. Override when installing the plugin or leave as a default for sensible log levels.
 * Log level may be represented using a system log level, such as on JS, Wasm, Android and iOS.
 * On other platforms, log level will be represented as an emoji ([StoreLogLevel.asSymbol]).
 */
public enum class StoreLogLevel {

    Trace, Debug, Info, Warn, Error
}
