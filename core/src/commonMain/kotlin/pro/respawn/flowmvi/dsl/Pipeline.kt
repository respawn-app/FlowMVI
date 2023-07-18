package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Recoverable
import pro.respawn.flowmvi.api.StateReceiver
import kotlin.coroutines.CoroutineContext

@Suppress("FunctionName")
internal fun Recoverable.PipelineExceptionHandler() = CoroutineExceptionHandler { ctx, e ->
    if (e !is Exception) throw e
    ctx[PipelineContext.Key]?.launch { recover(e) } ?: throw e
}

@FlowMVIDSL
internal fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.pipeline(
    scope: CoroutineScope,
    block: suspend PipelineContext<S, I, A>.() -> Unit
): Job where T : IntentReceiver<I>, T : StateReceiver<S>, T : ActionReceiver<A>, T : Recoverable {
    val pipeline = object :
        PipelineContext<S, I, A>,
        IntentReceiver<I> by this,
        StateReceiver<S> by this,
        ActionReceiver<A> by this {
        override val key: CoroutineContext.Key<*> = PipelineContext.Key
        private val handler = PipelineExceptionHandler()

        override fun launch(
            context: CoroutineContext,
            start: CoroutineStart,
            block: suspend CoroutineScope.() -> Unit
        ) = scope.launch(context + this + handler, start, block)

        override fun <T> async(
            context: CoroutineContext,
            start: CoroutineStart,
            block: suspend CoroutineScope.() -> T
        ) = scope.async(context + this + handler, start, block)
    }
    return pipeline.launch {
        block(pipeline)
    }
}
