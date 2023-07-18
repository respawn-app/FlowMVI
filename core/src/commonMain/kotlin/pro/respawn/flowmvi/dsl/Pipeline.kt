package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.DelicateStoreApi
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
        ActionReceiver<A> by this,
        CoroutineScope by (scope + SupervisorJob() + PipelineExceptionHandler()) {
        override val key: CoroutineContext.Key<*> = PipelineContext.Key
    }
    return pipeline.launch {
        withContext(pipeline) {
            block(pipeline)
        }
    }
}
