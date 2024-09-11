package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin

private data object NoOpPlugin : StorePlugin<Nothing, Nothing, Nothing> {

    override val name: String? = null
}

/**
 * A plugin that does nothing. Useful for testing or mocking
 */
@Suppress("UNCHECKED_CAST")
public fun <S : MVIState, I : MVIIntent, A : MVIAction> NoOpPlugin(): StorePlugin<S, I, A> =
    NoOpPlugin as StorePlugin<S, I, A>
