package pro.respawn.flowmvi.logging

/**
 * A [StoreLogger] that does nothing.
 */
public val NoOpStoreLogger: StoreLogger by lazy { StoreLogger { _, _, _ -> } }
