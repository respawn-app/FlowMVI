package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import kotlin.coroutines.coroutineContext

/**
 * Obtain the pipeline context of the current coroutine, if present, and cast it to types [S], [I], and [A]
 */
@Suppress("UNCHECKED_CAST")
@DelicateStoreApi
public suspend inline fun <S : MVIState, I : MVIIntent, A : MVIAction> pipelineContext(): PipelineContext<S, I, A>? =
    coroutineContext[PipelineContext] as? PipelineContext<S, I, A>?
