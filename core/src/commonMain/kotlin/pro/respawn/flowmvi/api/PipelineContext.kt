package pro.respawn.flowmvi.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@FlowMVIDSL
public interface PipelineContext<S : MVIState, in I : MVIIntent, in A : MVIAction> :
    IntentReceiver<I>,
    StateReceiver<S>,
    ActionReceiver<A>,
    CoroutineContext.Element {

    public fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job

    public fun <T> async(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> T
    ): Deferred<T>

    public companion object Key : CoroutineContext.Key<PipelineContext<*, *, *>>
}
