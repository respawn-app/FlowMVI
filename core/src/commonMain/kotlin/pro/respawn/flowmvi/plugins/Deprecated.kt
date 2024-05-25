package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.LazyPlugin
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.logging.StoreLogLevel

/**
 * A logging plugin that prints logs to the console using [println]. Tag is not used except for naming the plugin.
 * @see loggingPlugin
 */
@FlowMVIDSL
@Deprecated(
    "Just use logging plugin from now on",
    ReplaceWith("loggingPlugin(tag, name, level)")
)
public fun <S : MVIState, I : MVIIntent, A : MVIAction> consoleLoggingPlugin(
    tag: String? = null,
    level: StoreLogLevel? = null,
): LazyPlugin<S, I, A> = loggingPlugin(tag, level)

/**
 * Creates a [loggingPlugin] that is suitable for each targeted platform.
 * This plugin will log to:
 * * Logcat on Android,
 * * NSLog on apple targets,
 * * console.log on JS,
 * * stdout on mingw/native
 * * System.out on JVM.
 */
@Deprecated(
    "Just use logging plugin from now on",
    ReplaceWith("loggingPlugin(tag = tag, level = level)")
)
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> platformLoggingPlugin(
    tag: String? = null,
    level: StoreLogLevel? = null
): LazyPlugin<S, I, A> = loggingPlugin(tag, level)
