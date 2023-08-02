package pro.respawn.flowmvi.modules

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StateReceiver
import kotlin.coroutines.CoroutineContext

internal interface PipelineSubscriber<S : MVIState, I : MVIIntent, A : MVIAction> :
    Recoverable<S, I, A>,
    StateReceiver<S>,
    IntentReceiver<I> {

    suspend fun PipelineContext<S, I, A>.onAction(action: A)
    suspend fun PipelineContext<S, I, A>.onTransformState(transform: suspend S.() -> S)
    fun PipelineContext<S, I, A>.onStart()
    fun onStop(e: Exception?)
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
internal inline fun <S : MVIState, I : MVIIntent, A : MVIAction> PipelineSubscriber<S, I, A>.launchPipeline(
    name: String?,
    parent: CoroutineScope,
): Job = object :
    PipelineContext<S, I, A>,
    IntentReceiver<I> by this,
    StateReceiver<S> by this,
    ActionReceiver<A> {
    private val job = SupervisorJob(parent.coroutineContext[Job])
    private val handler = PipelineExceptionHandler(this)
    private val pipelineName = CoroutineName("${name}PipelineContext")
    override val coroutineContext: CoroutineContext = parent.coroutineContext + pipelineName + this + job + handler
    override suspend fun updateState(transform: suspend S.() -> S) = onTransformState(transform)
    override suspend fun send(action: A) = onAction(action)
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
