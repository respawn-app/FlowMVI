@file:MustUseReturnValues

package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.api.context.ShutdownContext
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin

/**
 * Alias for [StorePlugin.onStop] callback or `plugin { onStop { block() } }`
 *
 * See the function documentation for more info.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> deinitPlugin(
    crossinline block: ShutdownContext<S, I, A>.(e: Exception?) -> Unit
): StorePlugin<S, I, A> = plugin { onStop { block(it) } }

/**
 * Install a new [deinitPlugin].
 */
@IgnorableReturnValue
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.deinit(
    crossinline block: ShutdownContext<S, I, A>.(e: Exception?) -> Unit
): Unit = install(deinitPlugin(block))
