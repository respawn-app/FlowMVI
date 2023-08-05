package pro.respawn.flowmvi.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch

/**
 * Catches exceptions only, rethrowing any throwables
 * @see Flow.catch
 */
public inline fun <T> Flow<T>.catchExceptions(
    crossinline block: suspend FlowCollector<T>.(Exception) -> Unit
): Flow<T> = catch { throwable -> (throwable as? Exception)?.let { block(it) } ?: throw throwable }
