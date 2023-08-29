package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder

/**
 * Create a [StorePlugin] that simply consumes intents and does nothing with them.
 * This is useful when you are using [reducePlugin] with `consume = false`
 * You can add this to the end of your store declaration to consume the remaining unprocessed intents if you are
 * **sure** that you already handled all of them fully
 *
 * @see reducePlugin
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> consumeIntentsPlugin(): StorePlugin<S, I, A> = reducePlugin { }

/**
 * Create and install a plugin that consumes intents and does nothing with them.
 *
 * This is useful when you are using [reducePlugin] with `consume = false`
 * You can add this to the end of your store declaration to consume the remaining unprocessed intents if you are
 * **sure** that you already handled all of them fully
 * @see consumeIntents
 * @see reduce
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.consumeIntents(): Unit = reduce { }
