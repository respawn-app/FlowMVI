package pro.respawn.flowmvi.plugins

import android.util.Log
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.LazyPlugin
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.logging.asStoreLogLevel

/**
 * Create a new [loggingPlugin] that prints using android's [Log].
 */
@Deprecated(
    "Just use logging plugin",
    ReplaceWith("loggingPlugin(tag = tag, level =  level)")
)
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> androidLoggingPlugin(
    tag: String? = null,
    level: Int? = null,
): LazyPlugin<S, I, A> = loggingPlugin(tag = tag, level = level?.asStoreLogLevel)

/**
 * Create a new [loggingPlugin] that prints using android's [Log].
 */
@Deprecated(
    "Just use logging plugin",
    ReplaceWith("enableLogging(name = name, level = level)")
)
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.androidLoggingPlugin(
    name: String? = null,
    level: Int? = null,
): Unit = loggingPlugin<S, I, A>(tag = name, level = level?.asStoreLogLevel).let(::install)
