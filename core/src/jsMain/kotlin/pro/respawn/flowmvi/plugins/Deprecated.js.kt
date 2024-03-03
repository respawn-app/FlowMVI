package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.logging.PlatformStoreLogger
import pro.respawn.flowmvi.logging.StoreLogLevel

/**
 * Creates a [loggingPlugin] that prints to [kotlin.js.Console]
 */
@Deprecated(
    "Just use logging plugin with PlatformLogger",
    ReplaceWith("loggingPlugin(PlatformStoreLogger, tag = tag, level =  level)")
)
public fun <S : MVIState, I : MVIIntent, A : MVIAction> consoleLoggingPlugin(
    tag: String?,
    level: StoreLogLevel?
): StorePlugin<S, I, A> = loggingPlugin(PlatformStoreLogger, tag, level = level)
