package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.PipelineContext

context(PipelineContext<*, I, *>)
public fun <I : MVIIntent> I.send(): Unit = intent(this)

context(PipelineContext<*, *, A>)
public fun <A : MVIAction> A.send(): Unit = action(this)

context(PipelineContext<*, I, *>)
public suspend fun <I : MVIIntent> I.emit(): Unit = emit(this)

context(PipelineContext<*, *, A>)
public suspend fun <A : MVIAction> A.emit(): Unit = emit(this)
