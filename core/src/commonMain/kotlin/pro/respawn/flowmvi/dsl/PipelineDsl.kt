package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import kotlin.coroutines.coroutineContext

/**
 * Obtain the pipeline context of the current coroutine, if present, and cast it to types [S], [I], and [A]
 */
@Suppress("UNCHECKED_CAST")
@DelicateStoreApi
public suspend inline fun <S : MVIState, I : MVIIntent, A : MVIAction> pipelineContext(): PipelineContext<S, I, A>? =
    coroutineContext[PipelineContext] as? PipelineContext<S, I, A>?

/**
 * Invoke [handler] when the store stops.
 *
 * The biggest difference from [StorePlugin.onStop] is that this handler is attached to the pipeline's job and thus:
 * * there's **no guarantee** on the order of invocations between **all** registered handlers and **any plugins**
 * * there's no guarantee as to which **thread or coroutine context** this will be invoked on.
 * * Implementation of [handler] must be fast, non-blocking, and thread-safe. This handler can be invoked concurrently with the surrounding code.
 *
 * As a rule, this can be useful for cleanup work that you can't put into a [StorePlugin.onStop] callback and that does
 * not reference any other plugins.
 *
 * @see Job.invokeOnCompletion
 */
@FlowMVIDSL
public fun PipelineContext<*, *, *>.onStop(
    handler: CompletionHandler
): DisposableHandle = coroutineContext.job.invokeOnCompletion(handler)
