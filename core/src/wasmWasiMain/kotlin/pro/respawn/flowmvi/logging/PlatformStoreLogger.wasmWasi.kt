package pro.respawn.flowmvi.logging

/**
 * A [StoreLogger] instance for the WASM/WASI platform.
 *
 * Uses [ConsoleStoreLogger] to output logs to the JavaScript console
 * when running in a browser environment.
 */
public actual val PlatformStoreLogger: StoreLogger get() = ConsoleStoreLogger
