@file:pro.respawn.flowmvi.annotation.MustUseReturnValues

package pro.respawn.flowmvi.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.runningFold

/**
 * Catches exceptions only, rethrowing any throwables
 * @see Flow.catch
 */
public inline fun <T> Flow<T>.catchExceptions(
    crossinline block: suspend FlowCollector<T>.(Exception) -> Unit
): Flow<T> = catch { throwable -> (throwable as? Exception)?.let { block(it) } ?: throw throwable }

internal fun <T> Flow<T>.withPrevious(
    initial: T
) = runningFold(initial to initial) { (_, prev), current -> prev to current }
