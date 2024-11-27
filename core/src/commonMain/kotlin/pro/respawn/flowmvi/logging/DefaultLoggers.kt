package pro.respawn.flowmvi.logging

/**
 * A [StoreLogger] that does nothing.
 */
public val NoOpStoreLogger: StoreLogger by lazy { StoreLogger { _, _, _ -> } }

/**
 * A logger that prints to console ([println])
 */
public val ConsoleStoreLogger: StoreLogger by lazy {
    StoreLogger { level, tag, message -> println(template(level, tag, message)) }
}

/**
 * A platform-specific [StoreLogger] implementation that uses the current OS's log stream.
 * This logger should be used as the default choice for platform-specific logging requirements.
 *
 * @see NoOpStoreLogger for no-op logging
 * @see ConsoleStoreLogger for basic console output
 */
public expect val PlatformStoreLogger: StoreLogger
