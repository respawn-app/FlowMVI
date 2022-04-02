package com.nek12.flowMVI

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

val <S : MVIState> MVIProvider<S, *, *>.currentState: S get() = states.value

fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIView<S, I, A>.subscribe(
    scope: CoroutineScope,
) {
    scope.launch { provider.states.collect { render(it) } }
    scope.launch { provider.actions.collect { consume(it) } }
}

inline fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIStore<S, I, A>.subscribe(
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
inline fun <reified T : S, reified S : MVIState> MVIProvider<S, *, *>.withState(block: T.() -> T): S {
    return (currentState as? T)?.let(block) ?: currentState
}
