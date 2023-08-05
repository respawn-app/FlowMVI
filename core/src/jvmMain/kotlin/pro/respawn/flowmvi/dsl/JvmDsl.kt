package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.PipelineContext

/**
 * An alias for [PipelineContext.send]
 */
context(PipelineContext<*, I, *>)
public fun <I : MVIIntent> I.send(): Unit = intent(this)

/**
 * An alias for [PipelineContext.action]
 */
context(PipelineContext<*, *, A>)
public suspend fun <A : MVIAction> A.send(): Unit = action(this)

/**
 * An alias for [PipelineContext.emit]
 */
context(PipelineContext<*, I, *>)
public suspend fun <I : MVIIntent> I.emit(): Unit = emit(this)

/**
 * An alias for [PipelineContext.action]
 */
context(PipelineContext<*, *, A>)
public suspend fun <A : MVIAction> A.emit(): Unit = emit(this)
