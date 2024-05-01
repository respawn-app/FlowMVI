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
