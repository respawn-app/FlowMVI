package pro.respawn.flowmvi.logging

/**
 * Alias for [ConsoleStoreLogger]
 */
public actual val PlatformStoreLogger: StoreLogger get() = ConsoleStoreLogger
