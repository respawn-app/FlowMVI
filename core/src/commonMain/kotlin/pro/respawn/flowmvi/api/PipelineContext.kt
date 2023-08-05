package pro.respawn.flowmvi.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.job
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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
 * The pipeline's context is always the context the store was started with.
 */
@FlowMVIDSL
public interface PipelineContext<S : MVIState, I : MVIIntent, A : MVIAction> :
    IntentReceiver<I>,
    StateReceiver<S>,
    ActionReceiver<A>,
    CoroutineScope,
    CoroutineContext.Element {

    /**
     * Same as [cancel], but for resolving the ambiguity between context.cancel() and scope.cancel()
     */
    public fun close(): Unit = coroutineContext.job.cancel()

    /**
     * An alias for [Flow.collect] that does not override the context, amending it instead.
     * Use as a safer alternative to [Flow.flowOn] and then [Flow.collect]
     */
    public suspend fun <T> Flow<T>.consume(context: CoroutineContext = EmptyCoroutineContext): Unit =
        flowOn(this@PipelineContext + context).collect()

    /**
     * Catch exceptions in the upstream flow (everything above this call) and call [PipelineContext.recover] on them.
     */
    // public suspend fun <T> Flow<T>.recover(): Flow<T> = catchExceptions { recover(it) }

    /**
     * A key of the [PipelineContext] in the parent coroutine context.
     */
    @DelicateStoreApi
    public companion object : CoroutineContext.Key<PipelineContext<*, *, *>>

    @DelicateStoreApi
    override val key: CoroutineContext.Key<*> get() = PipelineContext
}
