package pro.respawn.flowmvi.logging

/**
 * A [StoreLogger] implementation that prints to [System.out]
 */
public actual val PlatformStoreLogger: StoreLogger by lazy {
    StoreLogger { level, tag, message ->
        when (level) {
            StoreLogLevel.Trace, StoreLogLevel.Debug, StoreLogLevel.Info -> println(template(level, tag, message))
            StoreLogLevel.Warn, StoreLogLevel.Error -> System.err.println(template(level, tag, message))
        }
    }
}
