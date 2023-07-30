package pro.respawn.flowmvi.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.CoroutineContext

/**
 * PipelineContext is an entity that exposes the underlying logic of the [Store] to its [StorePlugin]s.
 *
 * Context is an implementation of the following:
 * * [IntentReceiver]
 * * [StateReceiver]
 * * [ActionReceiver]
 * * [CoroutineScope]
 *
 * Pipeline context is built based on the parent [CoroutineScope] and depends on it.
 * Pipeline context is a child of the [CoroutineContext] of the store (scope).
 *
 * To be used, this context **must not be overridden** by the store's logic.
 * Writing the following:
 * ```kotlin
 * withContext(Dispatchers.IO) { send(Intent) }
 * ```
 * **will result in an exception**
 * Instead, if you want to send intents using the context, use it as follows:
 * ```kotlin
 * // this -> PipelineContext<S, I, A>
 * withContext(this + Dispatchers.IO) { }
 * ```
 * **The pipeline context's scope is not necessarily the scope that the [Store] was [Store.start]ed with.**
 */
@FlowMVIDSL
public interface PipelineContext<S : MVIState, in I : MVIIntent, in A : MVIAction> :
    IntentReceiver<I>,
    StateReceiver<S>,
    ActionReceiver<A>,
    CoroutineContext.Element,
    CoroutineScope {

    /**
     * An alias for [Flow.collect] that does not override the context amending it instead.
     * Use as a safer alternative to [Flow.flowOn]
     */
    public suspend fun <T> Flow<T>.consume(context: CoroutineContext = Dispatchers.Default): Unit =
        flowOn(this@PipelineContext + Dispatchers.Default).collect()

    /**
     * A key of the [PipelineContext] in the parent coroutine context.
     */
    @DelicateStoreApi
    public companion object Key : CoroutineContext.Key<PipelineContext<*, *, *>>
}
