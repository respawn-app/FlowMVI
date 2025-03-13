package pro.respawn.flowmvi.modules

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.UnrecoverableException
import pro.respawn.flowmvi.exceptions.RecursiveRecoverException
import pro.respawn.flowmvi.exceptions.UnhandledStoreException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

/**
 * An entity that can [recover] from exceptions happening during its lifecycle. Most often, a [Store]
 */
internal class RecoverModule<S : MVIState, I : MVIIntent, A : MVIAction>(
    private val handler: (suspend PipelineContext<S, I, A>.(e: Exception) -> Exception?)?
) : CoroutineContext.Element {

    override val key: CoroutineContext.Key<*> get() = RecoverModule

    val hasHandler = handler != null

    suspend fun PipelineContext<S, I, A>.handle(e: Exception) {
        if (handler == null) throw UnhandledStoreException(config.name, e)
        handler.invoke(this@handle, e)?.let { throw UnhandledStoreException(config.name, it) }
    }

    companion object : CoroutineContext.Key<RecoverModule<*, *, *>>
}

private tailrec fun UnrecoverableException.unwrapRecursion(): Exception = when (val cause = cause) {
    null -> this
    this -> this // cause is the same exception
    is CancellationException -> throw cause
    is UnrecoverableException -> cause.unwrapRecursion()
    else -> cause
}

internal suspend inline fun alreadyRecovered() = coroutineContext.alreadyRecovered

internal inline val CoroutineContext.alreadyRecovered get() = this[RecoverModule] != null

/**
 * Run [block] catching any exceptions and invoking [recover]. This will add this [RecoverModule] key to the coroutine
 * context of the [recover] block.
 */
internal suspend inline fun <R, S : MVIState, I : MVIIntent, A : MVIAction> PipelineContext<S, I, A>.catch(
    recover: RecoverModule<S, I, A>,
    block: suspend () -> R
): R? = try {
    block()
} catch (expected: Exception) {
    when {
        expected is CancellationException || expected is UnrecoverableException -> throw expected
        !recover.hasHandler -> throw UnhandledStoreException(config.name, expected)
        alreadyRecovered() -> throw RecursiveRecoverException(config.name, expected)
        else -> withContext(recover) {
            recover.run { handle(expected) }
            null
        }
    }
}

internal fun <S : MVIState, I : MVIIntent, A : MVIAction> PipelineContext<S, I, A>.PipelineExceptionHandler(
    recover: RecoverModule<S, I, A>
) = CoroutineExceptionHandler { ctx, e ->
    when {
        e !is Exception || e is CancellationException -> throw e
        !recover.hasHandler -> throw UnhandledStoreException(config.name, e)
        e is UnrecoverableException -> throw e.unwrapRecursion()
        ctx.alreadyRecovered -> throw e
        // add Recoverable to the coroutine context
        // and handle the exception asynchronously to allow suspending inside recover
        // Do NOT use the "ctx" parameter here, as that coroutine context is already invalid and will not launch
        else -> launch(recover) { recover.run { handle(e) } }.invokeOnCompletion { cause ->
            if (cause != null && cause !is CancellationException) throw cause
        }
    }
}
