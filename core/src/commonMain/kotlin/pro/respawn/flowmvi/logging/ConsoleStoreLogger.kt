package pro.respawn.flowmvi.logging

/**
 * A logger that prints to console ([println])
 */
public val ConsoleStoreLogger: StoreLogger by lazy {
    StoreLogger { level, tag, message -> println(template(level, tag, message)) }
}
