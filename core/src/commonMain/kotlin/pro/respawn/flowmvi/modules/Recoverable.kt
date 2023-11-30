package pro.respawn.flowmvi.modules

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Recoverable

@OptIn(DelicateStoreApi::class)
@Suppress("FunctionName")
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> Recoverable<S, I, A>.PipelineExceptionHandler(
    pipeline: PipelineContext<S, I, A>,
) = CoroutineExceptionHandler { ctx, e ->
    when {
        e !is Exception -> throw e
        ctx[Recoverable] != null -> throw e
        // add Recoverable to the coroutine context
        // and handle the exception asynchronously to allow suspending inside recover
        else -> with(pipeline) { launch { recover(e) } }
    }
}
