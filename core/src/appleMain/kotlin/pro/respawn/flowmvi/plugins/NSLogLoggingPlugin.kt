package pro.respawn.flowmvi.plugins

import platform.Foundation.NSLog
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin

public actual fun <S : MVIState, I : MVIIntent, A : MVIAction> platformLoggingPlugin(
    tag: String?,
    level: StoreLogLevel?,
): StorePlugin<S, I, A> = nativeLoggingPlugin(tag)

/**
 * Log store's events using [NSLog]
 * @see [loggingPlugin]
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> nativeLoggingPlugin(
    name: String? = null
): StorePlugin<S, I, A> = loggingPlugin(name) { _, tag, msg ->
    NSLog("%s: %s", tag, msg)
}
