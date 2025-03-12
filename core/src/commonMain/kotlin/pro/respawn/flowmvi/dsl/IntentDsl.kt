@file:Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")

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
    first: I,
    vararg other: I
) {
    intent(first)
    other.forEach(::intent)
}

/**
 * Alias for [IntentReceiver.emit] for multiple intents
 *
 * @see IntentReceiver.emit
 */
public suspend inline fun <I : MVIIntent> IntentReceiver<I>.emit(
    first: I,
    vararg other: I
) {
    emit(first)
    other.forEach { emit(it) }
}

/**
 * Alias for [IntentReceiver.intent] for multiple intents
 *
 * @see IntentReceiver.intent
 */
public inline fun <I : MVIIntent> IntentReceiver<I>.send(
    first: I,
    vararg other: I
): Unit = intent(first, other = other)

// ----- actions  -----

/**
 * Alias for [ActionReceiver.action] for multiple actions
 *
 * @see ActionReceiver.action
 */
public suspend inline fun <A : MVIAction> ActionReceiver<A>.action(
    first: A,
    vararg other: A
) {
    action(first)
    other.forEach { action(it) }
}

/**
 * Alias for [ActionReceiver.action] for multiple actions
 *
 * @see ActionReceiver.action
 */
public suspend inline fun <A : MVIAction> ActionReceiver<A>.emit(
    first: A,
    vararg other: A
): Unit = action(first, other = other)

/**
 * Alias for [ActionReceiver.send] for multiple actions
 *
 * @see ActionReceiver.send
 */
@DelicateStoreApi
public inline fun <A : MVIAction> ActionReceiver<A>.send(
    first: A,
    vararg other: A
) {
    send(first)
    other.forEach(::send)
}
