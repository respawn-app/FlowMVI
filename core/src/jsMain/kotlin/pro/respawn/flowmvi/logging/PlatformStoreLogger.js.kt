package pro.respawn.flowmvi.logging

/**
 * Logger that prints to [console].
 */
public actual val PlatformStoreLogger: StoreLogger by lazy {
    StoreLogger { level, tag, message ->
        val template = "${if (tag.isNullOrBlank()) "" else "$tag: "}${message()}"
        with(console) {
            when (level) {
                StoreLogLevel.Trace, StoreLogLevel.Debug -> log(template)
                StoreLogLevel.Info -> info(template)
                StoreLogLevel.Warn -> warn(template)
                StoreLogLevel.Error -> error(template)
            }
        }
    }
}
