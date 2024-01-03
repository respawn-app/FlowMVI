package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent

// ----- intents -----

public inline fun <I : MVIIntent> IntentReceiver<I>.intent(
    vararg intents: I
): Unit = intents.forEach(::intent)

public inline fun <I : MVIIntent> IntentReceiver<I>.send(
    vararg intents: I
): Unit = intent(intents = intents)

public suspend inline fun <I : MVIIntent> IntentReceiver<I>.emit(
    vararg intents: I
): Unit = intents.forEach { emit(it) }

// ----- actions  -----

public suspend inline fun <A : MVIAction> ActionReceiver<A>.action(
    vararg actions: A
): Unit = actions.forEach { action(it) }

public suspend inline fun <A : MVIAction> ActionReceiver<A>.emit(
    vararg actions: A
): Unit = action(actions = actions)

@DelicateStoreApi
public inline fun <A : MVIAction> ActionReceiver<A>.send(
    vararg actions: A
): Unit = actions.forEach(::send)
