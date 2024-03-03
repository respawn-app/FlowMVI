package pro.respawn.flowmvi.logging

/**
 * A [StoreLogger] implementation that prints to [System.out]
 */
public actual val PlatformStoreLogger: StoreLogger by lazy {
    StoreLogger { level, tag, message ->
        val msg = "${if (tag != null) "$tag: " else ""}${message()}"
        when (level) {
            StoreLogLevel.Trace, StoreLogLevel.Debug, StoreLogLevel.Info -> println(msg)
            StoreLogLevel.Warn, StoreLogLevel.Error -> System.err.println(msg)
        }
    }
}
