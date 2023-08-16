package pro.respawn.flowmvi.plugins

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
public expect fun <S : MVIState, I : MVIIntent, A : MVIAction> platformLoggingPlugin(
    tag: String? = null,
    level: StoreLogLevel? = null
): StorePlugin<S, I, A>
