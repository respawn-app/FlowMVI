package pro.respawn.flowmvi.logging

/**
 * Log level of this store. Override when installing the plugin or leave as a default for sensible log levels.
 * Log level may be represented using a system log level, such as on JS, Wasm, Android and iOS.
 * On other platforms, log level will be represented as an emoji ([StoreLogLevel.asSymbol]).
 */
public enum class StoreLogLevel {

    /**
     * Trace level (also called Verbose) corresponds to messages intended for development. They are hidden by default
     * and this level allows for any spam amount. Should only be used for development as log spam can affect
     * performance. The library does not print to this level except for tests by default.
     */
    Trace,

    /**
     * Debug is the level used for development. It should not be sent to production as it can leak sensitive data.
     * The library uses this level to log states by default.
     */
    Debug,

    /**
     * Info is the level for "everything is fine, just wanted you to know" log calls. It can sometimes be sent to
     * production, such as Crashlytics. The library logs intents and actions to this level by default.
     */
    Info,

    /**
     * Warn is the level for errors that were recovered from. The library uses this level to print errors in internal
     * code. This level and above should usually be sent to metric or crash logging services.
     */
    Warn,

    /**
     * Error is the level for critical failures. Use this sparingly and only to report things where stuff went
     * seriously sideways. The library reports errors with this level by default.
     */
    Error
}
