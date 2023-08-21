package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.Consumer
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent

/**
 * An alias for [IntentReceiver.send]
 */
context(IntentReceiver<I>)
public fun <I : MVIIntent> I.send(): Unit = intent(this)

/**
 * An alias for [ActionReceiver.action]
 */
context(ActionReceiver<A>)
public suspend fun <A : MVIAction> A.send(): Unit = action(this)

/**
 * An alias for [IntentReceiver.emit]
 */
context(IntentReceiver<I>)
public suspend fun <I : MVIIntent> I.emit(): Unit = emit(this)

/**
 * An alias for [ActionReceiver.action]
 */
context(ActionReceiver<A>)
public suspend fun <A : MVIAction> A.emit(): Unit = emit(this)

/**
 * An alias for [ActionReceiver.action]
 */
context(Consumer<*, I, *>)
public fun <I : MVIIntent> I.send(): Unit = send(this)
