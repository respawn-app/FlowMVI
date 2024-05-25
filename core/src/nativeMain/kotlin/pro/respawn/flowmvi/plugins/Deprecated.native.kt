package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.LazyPlugin
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

/**
 * Log store's events using platform logger.
 * @see [loggingPlugin]
 */
@Deprecated(
    "Just use logging plugin",
    ReplaceWith("loggingPlugin(tag = tag, name = name)")
)
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> nativeLoggingPlugin(
    tag: String? = null,
): LazyPlugin<S, I, A> = loggingPlugin(tag = tag)
