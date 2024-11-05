package pro.respawn.flowmvi.logging

/**
 * A [StoreLogger] instance for each supported platform
 */
public actual val PlatformStoreLogger: StoreLogger get() = ConsoleStoreLogger
