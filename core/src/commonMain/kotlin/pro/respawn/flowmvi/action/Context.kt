package pro.respawn.flowmvi.action

import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.base.PipelineContext as BaseContext

public interface PipelineContext<S : MVIState, in I : MVIIntent, in A : MVIAction> :
    BaseContext<S, I>,
    ActionReceiver<A>

public interface SuspendPipelineContext<S : MVIState, I : MVIIntent, A : MVIAction> :
    PipelineContext<S, I, A>,
    CoroutineScope

internal fun <S : MVIState, I : MVIIntent, A : MVIAction> PipelineContext<S, I, A>.pipeline(
    scope: CoroutineScope
): SuspendPipelineContext<S, I, A> = object :
    SuspendPipelineContext<S, I, A>,
    CoroutineScope by scope,
    PipelineContext<S, I, A> by this {}
