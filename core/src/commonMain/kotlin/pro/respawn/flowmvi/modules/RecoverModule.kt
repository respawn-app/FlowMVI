package pro.respawn.flowmvi.modules

import kotlinx.coroutines.CoroutineExceptionHandler
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import kotlin.coroutines.CoroutineContext

/**
 * An entity that can [recover] from exceptions happening during its lifecycle. Most often, a [Store]
 */
@Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION") // https://youtrack.jetbrains.com/issue/KTIJ-7642
internal fun interface Recoverable<S : MVIState, I : MVIIntent, A : MVIAction> : CoroutineContext.Element {

    override val key: CoroutineContext.Key<*> get() = Recoverable

    fun PipelineContext<S, I, A>.recover(e: Exception)

    companion object : CoroutineContext.Key<Recoverable<*, *, *>>
}

@Suppress("FunctionName")
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> Recoverable<S, I, A>.PipelineExceptionHandler(
    pipeline: PipelineContext<S, I, A>,
) = CoroutineExceptionHandler { ctx, e ->
    when {
        e !is Exception -> throw e
        ctx[Recoverable] != null -> throw e
        else -> with(pipeline) { recover(e) }
    }
}
