package pro.respawn.flowmvi.logging

import platform.Foundation.NSLog

/**
 * Alias for [ConsoleStoreLogger]
 */
public actual val PlatformStoreLogger: StoreLogger by lazy {
    StoreLogger { level, tag, message -> NSLog(template(level, tag, message)) }
}
