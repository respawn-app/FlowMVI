package com.nek12.flowMVI

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.experimental.ExperimentalTypeInference

val <S : MVIState> MVIProvider<S, *, *>.currentState: S get() = states.value

fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIView<S, I, A>.subscribe(
    scope: CoroutineScope,
) {
    scope.launch { provider.states.collect { render(it) } }
    scope.launch { provider.actions.collect { consume(it) } }
}

/**
 * Execute [block] if current state is [T] else just return [currentState].
 */
inline fun <reified T : S, reified S: MVIState> MVIProvider<S, *, *>.withState(block: T.() -> T): S {
    return (currentState as? T)?.let(block) ?: currentState
}
