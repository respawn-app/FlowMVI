@file:Suppress("NOTHING_TO_INLINE")

package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent

// ----- intents -----
/**
 * Alias for [IntentReceiver.intent] for multiple intents
 *
 * @see IntentReceiver.intent
 */
public inline fun <I : MVIIntent> IntentReceiver<I>.intent(
    vararg intents: I
): Unit = intents.forEach(::intent)

/**
 * Alias for [IntentReceiver.intent] for multiple intents
 *
 * @see IntentReceiver.intent
 */
public inline fun <I : MVIIntent> IntentReceiver<I>.send(
    vararg intents: I
): Unit = intent(intents = intents)

/**
 * Alias for [IntentReceiver.intent]
 */
public fun <I : MVIIntent> IntentReceiver<I>.send(intent: I): Unit = intent(intent)

/**
 * Alias for [IntentReceiver.emit] for multiple intents
 *
 * @see IntentReceiver.emit
 */
public suspend inline fun <I : MVIIntent> IntentReceiver<I>.emit(
    vararg intents: I
): Unit = intents.forEach { emit(it) }

// ----- actions  -----
/**
 * Alias for [ActionReceiver.action] for multiple actions
 *
 * @see ActionReceiver.action
 */
public suspend inline fun <A : MVIAction> ActionReceiver<A>.action(
    vararg actions: A
): Unit = actions.forEach { action(it) }

/**
 * Alias for [ActionReceiver.action] for multiple actions
 *
 * @see ActionReceiver.action
 */
public suspend inline fun <A : MVIAction> ActionReceiver<A>.emit(
    vararg actions: A
): Unit = action(actions = actions)

/**
 * Alias for [ActionReceiver.send] for multiple actions
 *
 * @see ActionReceiver.send
 */
@DelicateStoreApi
public inline fun <A : MVIAction> ActionReceiver<A>.send(
    vararg actions: A
): Unit = actions.forEach(::send)

/**
 * Alias for [ActionReceiver.action]
 */
public suspend inline fun <A : MVIAction> ActionReceiver<A>.emit(action: A): Unit = action(action)
