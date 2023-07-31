
package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.newCoroutineContext
import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StateReceiver
import pro.respawn.flowmvi.modules.PipelineExceptionHandler
import pro.respawn.flowmvi.modules.Recoverable

@FlowMVIDSL
internal inline fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.pipeline(
    name: String?,
    scope: CoroutineScope,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend PipelineContext<S, I, A>.() -> Unit
): Job where T : IntentReceiver<I>, T : StateReceiver<S>, T : ActionReceiver<A>, T : Recoverable<S, I, A> {
    val pipeline = object :
        PipelineContext<S, I, A>,
        IntentReceiver<I> by this,
        StateReceiver<S> by this,
        ActionReceiver<A> by this {
        private val handler = PipelineExceptionHandler(this)
        private val pipelineName = CoroutineName("${name}PipelineContext")

        // Coroutine context consists of the following: job, name, handler, dispatcher
        // here we create a new child coroutine context
        // * using coroutine name of the store
        // using exception handler that uses the scope to recover from unhandled exceptions
        // and we add the pipeline to the context to retrieve it from the child context
        override val coroutineContext = scope.newCoroutineContext(
            this + handler + pipelineName + SupervisorJob(scope.coroutineContext.job)
        )
    }

    // we launch a new job using the pipeline's context as a parent
    // thus we are using a child job of the scope and everything else is custom
    return pipeline.launch(start = start) { block(pipeline) }
}
