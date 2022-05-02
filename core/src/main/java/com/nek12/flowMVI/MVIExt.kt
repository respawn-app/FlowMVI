package com.nek12.flowMVI

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

val <S: MVIState> MVIProvider<S, *, *>.currentState: S get() = states.value

fun <S: MVIState, I: MVIIntent, A: MVIAction> MVIView<S, I, A>.subscribe(
    scope: CoroutineScope,
) {
    scope.launch { provider.states.collect { render(it) } }
    scope.launch { provider.actions.collect { consume(it) } }
}

inline fun <S: MVIState, I: MVIIntent, A: MVIAction> MVIStore<S, I, A>.subscribe(
    scope: CoroutineScope,
    crossinline consume: (A) -> Unit,
    crossinline render: (S) -> Unit,
) {
    scope.launch {
        actions.collect { consume(it) }
    }

    scope.launch {
        states.collect { render(it) }
    }
}

/**
 * Execute [block] if current state is [T] else just return [currentState].
 */
inline fun <reified T: S, reified S: MVIState> MVIProvider<S, *, *>.withState(block: T.() -> S): S =
    (currentState as? T)?.let(block) ?: currentState

fun <S: MVIState> MVIStore<S, *, *>.launchForState(
    scope: CoroutineScope,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    recover: suspend CoroutineScope.(Exception) -> S = { throw it },
    block: suspend CoroutineScope.() -> S,
) = scope.launch(context, start) {
    set(
        try {
            block()
        } catch (e: Exception) {
            recover(e)
        }
    )
}
