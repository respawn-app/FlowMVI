package pro.respawn.flowmvi.logging

/**
 * A platform [StoreLogger] implementation that prints to console (alias for [ConsoleStoreLogger])
 */
public actual val PlatformStoreLogger: StoreLogger get() = ConsoleStoreLogger
