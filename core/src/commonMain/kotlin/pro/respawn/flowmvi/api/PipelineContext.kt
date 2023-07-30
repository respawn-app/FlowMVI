package pro.respawn.flowmvi.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.CoroutineContext

@FlowMVIDSL
public interface PipelineContext<S : MVIState, in I : MVIIntent, in A : MVIAction> :
    IntentReceiver<I>,
    StateReceiver<S>,
    ActionReceiver<A>,
    CoroutineContext.Element,
    CoroutineScope {

    public suspend fun <T> Flow<T>.consume(context: CoroutineContext = Dispatchers.Default): Unit =
        flowOn(this@PipelineContext + Dispatchers.Default).collect()

    public companion object Key : CoroutineContext.Key<PipelineContext<*, *, *>>
}
