package pro.respawn.flowmvi.logging

import android.util.Log

/**
 * Android-specific platform store logger that prints to logcat ([Log])
 */
public actual val PlatformStoreLogger: StoreLogger by lazy {
    StoreLogger { level, tag, block ->
        val message = block()
        chunkedLog(message) { Log.println(level.asLogPriority, tag ?: "Store", it) }
    }
}
