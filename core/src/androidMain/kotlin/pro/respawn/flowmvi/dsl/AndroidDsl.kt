package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent

/**
 * An alias for [IntentReceiver.send]
 */
context(receiver: IntentReceiver<I>) @FlowMVIDSL
public fun <I : MVIIntent> I.send(): Unit = receiver.intent(this)

/**
 * An alias for [ActionReceiver.action]
 */
context(receiver: ActionReceiver<A>) @FlowMVIDSL
public suspend fun <A : MVIAction> A.send(): Unit = receiver.action(this)

/**
 * An alias for [IntentReceiver.emit]
 */
context(receiver: IntentReceiver<I>) @FlowMVIDSL
public suspend fun <I : MVIIntent> I.emit(): Unit = receiver.emit(this)

/**
 * An alias for [ActionReceiver.action]
 */
context(receiver: ActionReceiver<A>) @FlowMVIDSL
public suspend fun <A : MVIAction> A.emit(): Unit = receiver.emit(this)
