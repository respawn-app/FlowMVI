package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StorePlugin

/**
 * A plugin that does nothing. Useful for testing or mocking
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> NoOpPlugin(): StorePlugin<S, I, A> = StorePlugin()
