package pro.respawn.flowmvi.logging

/**
 * A logger that prints to console ([println])
 */
public val ConsoleStoreLogger: StoreLogger by lazy {
    StoreLogger { level, tag, message -> println("${level.asSymbol} ${if (tag != null) "$tag: " else ""}${message()}") }
}
