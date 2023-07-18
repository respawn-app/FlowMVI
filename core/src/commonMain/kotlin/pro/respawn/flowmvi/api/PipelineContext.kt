package pro.respawn.flowmvi.api

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

@FlowMVIDSL
public interface PipelineContext<S : MVIState, in I : MVIIntent, in A : MVIAction> :
    IntentReceiver<I>,
    StateReceiver<S>,
    ActionReceiver<A>,
    CoroutineScope,
    CoroutineContext.Element {

    public companion object Key : CoroutineContext.Key<PipelineContext<*, *, *>>
}
