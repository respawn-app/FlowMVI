package pro.respawn.flowmvi.modules

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.modules.Recoverable.RecursiveRecoverException
import kotlin.coroutines.CoroutineContext

private const val RecursiveRecoverMessage = """
You have attempted to recover from an exception that was thrown when trying to recover from another exception.
This is not allowed because it would cause an infinite loop. Please make sure your recover function does not throw.
"""

/**
 * An entity that can [recover] from exceptions happening during its lifecycle. Most often, a [Store]
 */
@Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION") // https://youtrack.jetbrains.com/issue/KTIJ-7642
internal fun interface Recoverable<S : MVIState, I : MVIIntent, A : MVIAction> : CoroutineContext.Element {

    /**
     * An exception thrown when the [Recoverable.recover] is recursive (exception was thrown in the Exception handler).
     */
    class RecursiveRecoverException(cause: Exception) : IllegalStateException(RecursiveRecoverMessage, cause)

    override val key: CoroutineContext.Key<*> get() = Recoverable

    suspend fun PipelineContext<S, I, A>.recover(e: Exception)

    companion object : CoroutineContext.Key<Recoverable<*, *, *>>
}

@Suppress("FunctionName")
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> Recoverable<S, I, A>.PipelineExceptionHandler(
    pipeline: PipelineContext<S, I, A>,
) = CoroutineExceptionHandler { ctx, e ->
    when {
        e !is Exception -> throw e
        e is CancellationException || e.cause is CancellationException -> throw e
        ctx[Recoverable] != null -> throw RecursiveRecoverException(e)
        else -> with(pipeline) { launch { recover(e) } }
    }
}
