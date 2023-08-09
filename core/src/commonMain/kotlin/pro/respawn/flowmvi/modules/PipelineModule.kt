@file:OptIn(DelicateStoreApi::class)

package pro.respawn.flowmvi.modules

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Recoverable
import pro.respawn.flowmvi.api.StateReceiver
import kotlin.coroutines.CoroutineContext

// TODO: I don't like the api of this
//   there should be a way to provide pipeline context without creating a separate interface
internal interface PipelineModule<S : MVIState, I : MVIIntent, A : MVIAction> :
    Recoverable<S, I, A>,
    StateReceiver<S>,
    IntentReceiver<I> {

    suspend fun PipelineContext<S, I, A>.onAction(action: A)
    suspend fun PipelineContext<S, I, A>.onTransformState(transform: suspend S.() -> S)
}

/**
 * Coroutine context consists of the following: job, name, handler, dispatcher.
 * here we create a new child coroutine context:
 *
 * * using the dispatcher & other elements of the scope
 * * using a supervisor job that is a child of the parent scope's job
 * * using coroutine name of the store
 * * using exception handler that uses the scope itself to recover from unhandled exceptions
 * * using this pipeline instance as the context element
 */
@Suppress("Indentation")
internal inline fun <S : MVIState, I : MVIIntent, A : MVIAction> PipelineModule<S, I, A>.launchPipeline(
    name: String?,
    parent: CoroutineScope,
    crossinline onStop: (e: Exception?) -> Unit,
    onStart: PipelineContext<S, I, A>.() -> Unit,
): Job = object :
    IntentReceiver<I> by this,
    StateReceiver<S> by this,
    PipelineContext<S, I, A>,
    ActionReceiver<A> {

    override val key = PipelineContext // recoverable should be separate.
    private val job = SupervisorJob(parent.coroutineContext[Job])
    private val handler = PipelineExceptionHandler(this)
    private val pipelineName = CoroutineName("${name}PipelineContext")
    override val coroutineContext: CoroutineContext = parent.coroutineContext + pipelineName + this + job + handler
    override suspend fun updateState(transform: suspend S.() -> S) = onTransformState(transform)
    override suspend fun emit(action: A) = onAction(action)
    override fun send(action: A) {
        launch { onAction(action) }
    }
}.run {
    onStart()
    coroutineContext.job.apply {
        invokeOnCompletion {
            when (it) {
                null, is CancellationException -> onStop(null)
                !is Exception -> throw it
                else -> onStop(it)
            }
        }
    }
}
