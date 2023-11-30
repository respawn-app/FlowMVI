package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
/**
 * Creates a [loggingPlugin] that is suitable for each targeted platform.
 * This plugin will log to:
 * * Logcat on Android,
 * * NSLog on apple targets,
 * * console.log on JS,
 * * stdout on mingw/native
 * * System.out on JVM.
 */
@FlowMVIDSL
public actual fun <S : MVIState, I : MVIIntent, A : MVIAction> platformLoggingPlugin(
    tag: String?,
    level: StoreLogLevel?,
): StorePlugin<S, I, A> = loggingPlugin(tag) { logLevel: StoreLogLevel, _: String, msg: String ->
    when (level ?: logLevel) {
        StoreLogLevel.Trace, StoreLogLevel.Debug, StoreLogLevel.Info -> println(msg)
        StoreLogLevel.Warn, StoreLogLevel.Error -> System.err.println(msg)
    }
}
