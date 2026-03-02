@file:pro.respawn.flowmvi.annotation.MustUseReturnValues

package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.impl.plugin.PluginInstance

private val NoOpPlugin by lazy { PluginInstance<Nothing, Nothing, Nothing>() }

/**
 * A plugin that does nothing. Useful for testing or mocking
 */
@Suppress("UNCHECKED_CAST")
public fun <S : MVIState, I : MVIIntent, A : MVIAction> NoOpPlugin(): StorePlugin<S, I, A> =
    NoOpPlugin as StorePlugin<S, I, A>
