package pro.respawn.flowmvi.modules

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.api.UnrecoverableException
import pro.respawn.flowmvi.exceptions.RecursiveRecoverException
import pro.respawn.flowmvi.exceptions.UnhandledStoreException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

internal fun <S : MVIState, I : MVIIntent, A : MVIAction> recoverModule(
    delegate: StorePlugin<S, I, A>,
) = RecoverModule { e ->
    with(delegate) {
        if (e is UnrecoverableException) throw e
        onException(e)?.let { throw UnhandledStoreException(it) }
    }
}

/**
 * An entity that can [recover] from exceptions happening during its lifecycle. Most often, a [Store]
 */
@Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION") // https://youtrack.jetbrains.com/issue/KTIJ-7642
internal fun interface RecoverModule<S : MVIState, I : MVIIntent, A : MVIAction> : CoroutineContext.Element {

    override val key: CoroutineContext.Key<*> get() = RecoverModule

    suspend fun PipelineContext<S, I, A>.handle(e: Exception)

    /**
     * Run [block] catching any exceptions and invoking [recover]. This will add this [RecoverModule] key to the coroutine
     * context of the [recover] block.
     */
    suspend fun PipelineContext<S, I, A>.catch(block: suspend () -> Unit): Unit = try {
        withContext(this@RecoverModule) { block() }
    } catch (expected: Exception) {
        when {
            expected is CancellationException || expected is UnrecoverableException -> throw expected
            alreadyRecovered() -> throw RecursiveRecoverException(expected)
            else -> recover(expected)
        }
    }

    suspend fun PipelineContext<S, I, A>.recover(e: Exception) = withContext(this@RecoverModule) {
        handle(e)
    }

    @Suppress("FunctionName")
    fun PipelineContext<S, I, A>.PipelineExceptionHandler() = CoroutineExceptionHandler { ctx, e ->
        when {
            e !is Exception || e is CancellationException -> throw e
            e is UnrecoverableException -> throw e.unwrapRecursion()
            ctx.alreadyRecovered -> throw e
            // add Recoverable to the coroutine context
            // and handle the exception asynchronously to allow suspending inside recover
            // Do NOT use the "ctx" parameter here, as that coroutine context is already invalid and will not launch
            else -> launch(this@RecoverModule) { recover(e) }.invokeOnCompletion { cause ->
                if (cause != null && cause !is CancellationException) throw cause
            }
        }
    }

    companion object : CoroutineContext.Key<RecoverModule<*, *, *>>
}

private tailrec fun UnrecoverableException.unwrapRecursion(): Exception = when (val cause = cause) {
    null -> this
    this -> this // cause is the same exception
    is UnrecoverableException -> cause.unwrapRecursion()
    is CancellationException -> throw cause
    else -> cause
}

private suspend fun alreadyRecovered() = coroutineContext.alreadyRecovered

private val CoroutineContext.alreadyRecovered get() = this[RecoverModule] != null
