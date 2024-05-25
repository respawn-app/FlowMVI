package pro.respawn.flowmvi.util

import kotlin.contracts.contract

internal inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) {
        action(get(index))
    }
}

internal inline fun <T, R> List<T>.fastFold(initial: R, operation: (acc: R, T) -> R): R {
    contract { callsInPlace(operation) }
    var accumulator = initial
    fastForEach { e ->
        accumulator = operation(accumulator, e)
    }
    return accumulator
}
