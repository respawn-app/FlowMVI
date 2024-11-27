package pro.respawn.flowmvi.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import pro.respawn.flowmvi.annotation.NotIntendedForInheritance
import pro.respawn.flowmvi.api.lifecycle.StoreLifecycle
import pro.respawn.flowmvi.logging.StoreLogger
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
 * * [StoreLogger]
 *
 * Pipeline context is built based on the parent [CoroutineScope] and depends on it.
 * Pipeline context is a child of the [CoroutineContext] of the store (scope).
 * The pipeline's context is always the context the store was started with.
 */
@FlowMVIDSL
@OptIn(ExperimentalSubclassOptIn::class)
@SubclassOptInRequired(NotIntendedForInheritance::class)
public interface PipelineContext<S : MVIState, I : MVIIntent, A : MVIAction> :
    IntentReceiver<I>,
    StateReceiver<S>,
    ActionReceiver<A>,
    CoroutineScope,
    StoreLifecycle,
    CoroutineContext.Element {

    /**
     * The [StoreConfiguration] of this store
     */
    public val config: StoreConfiguration<S>

    /**
     * An alias for [Flow.collect] that does not override the context, amending it instead.
     * Use as a safer alternative to [Flow.flowOn] and then [Flow.collect]
     */
    public suspend fun <T> Flow<T>.consume(
        context: CoroutineContext = EmptyCoroutineContext
    ): Unit = flowOn(this@PipelineContext + context).collect()

    /**
     * A key of the [PipelineContext] in the parent coroutine context.
     */
    public companion object : CoroutineContext.Key<PipelineContext<*, *, *>>

    @DelicateStoreApi
    override val key: CoroutineContext.Key<*> get() = PipelineContext
}
