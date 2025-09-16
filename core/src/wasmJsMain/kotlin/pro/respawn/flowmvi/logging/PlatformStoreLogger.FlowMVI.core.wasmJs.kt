@file:Suppress("UnusedParameter", "UNUSED_PARAMETER")
@file:OptIn(ExperimentalWasmJsInterop::class)

package pro.respawn.flowmvi.logging

private fun log(message: String): Unit = js("""console.log(message)""")

private fun info(message: String): Unit = js("""console.info(message)""")

private fun warn(message: String): Unit = js("""console.warn(message)""")

private fun error(message: String): Unit = js("""console.error(message)""")

/**
 * A [StoreLogger] instance for each supported platform
 */
public actual val PlatformStoreLogger: StoreLogger = StoreLogger { level, tag, message ->
    val template = "${if (tag.isNullOrBlank()) "" else "$tag: "}${message()}"
    when (level) {
        StoreLogLevel.Trace, StoreLogLevel.Debug -> log(template)
        StoreLogLevel.Info -> info(template)
        StoreLogLevel.Warn -> warn(template)
        StoreLogLevel.Error -> error(template)
    }
}
