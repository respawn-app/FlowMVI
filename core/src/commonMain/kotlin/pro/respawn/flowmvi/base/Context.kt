package pro.respawn.flowmvi.base

import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState

public interface PipelineContext<S : MVIState, in I : MVIIntent> :
    IntentReceiver<I>,
    StateReceiver<S>

public interface SuspendPipelineContext<S : MVIState, in I : MVIIntent> :
    PipelineContext<S, I>,
    CoroutineScope

internal fun <S : MVIState, I : MVIIntent> PipelineContext<S, I>.pipeline(
    scope: CoroutineScope
): SuspendPipelineContext<S, I> = object :
    SuspendPipelineContext<S, I>,
    CoroutineScope by scope,
    PipelineContext<S, I> by this {}
