package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.logging.PlatformStoreLogger

/**
 * Log store's events using platform logger.
 * @see [loggingPlugin]
 */
@Deprecated(
    "Just use logging plugin with PlatformLogger",
    ReplaceWith("loggingPlugin(PlatformStoreLogger, name = name)")
)
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> nativeLoggingPlugin(
    name: String? = null
): StorePlugin<S, I, A> = loggingPlugin(PlatformStoreLogger, name)
