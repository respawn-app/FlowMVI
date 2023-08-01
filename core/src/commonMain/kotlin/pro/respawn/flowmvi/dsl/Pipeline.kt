
package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.newCoroutineContext
import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StateReceiver
import pro.respawn.flowmvi.modules.PipelineExceptionHandler
import pro.respawn.flowmvi.modules.Recoverable
import kotlin.coroutines.CoroutineContext

/**
 * Coroutine context consists of the following: job, name, handler, dispatcher.
 * here we create a new child coroutine context:
 * using the dispatcher & other elements of the scope
 * Using a supervisor job that is a child of the parent scope's job
 * using coroutine name of the store
 * using exception handler that uses the scope itself to recover from unhandled exceptions
 * and add the pipeline to the context to retrieve it from the child context
 */
@Suppress("Indentation")
internal fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.pipeline(
    name: String?,
    parent: CoroutineScope,
    onClose: (Exception?) -> Unit = {},
    onStart: PipelineContext<S, I, A>.() -> Unit,
): Job where T : IntentReceiver<I>, T : StateReceiver<S>, T : ActionReceiver<A>, T : Recoverable<S, I, A> {
    val job = SupervisorJob(parent.coroutineContext[Job])
    val pipeline = object :
        PipelineContext<S, I, A>,
        IntentReceiver<I> by this,
        StateReceiver<S> by this,
        ActionReceiver<A> by this {
        private val handler = PipelineExceptionHandler(this)
        private val pipelineName = CoroutineName("${name}PipelineContext")
        override val coroutineContext: CoroutineContext =
            parent.newCoroutineContext(pipelineName + this + job + handler)
    }
    pipeline.onStart()
    return job.apply {
        invokeOnCompletion {
            when (it) {
                null, is CancellationException -> onClose(null)
                !is Exception -> throw it
                else -> onClose(it)
            }
        }
    }
}
