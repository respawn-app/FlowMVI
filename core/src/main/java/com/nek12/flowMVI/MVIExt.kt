package com.nek12.flowMVI

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

val <S : MVIState, I : MVIIntent, A : MVIAction> MVIProvider<S, I, A>.currentState: S get() = states.value

/**
 * Subscribe to the store.
 * This function is __not__ lifecycle-aware and just uses provided scope for flow collection.
 */
fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIView<S, I, A>.subscribe(
    scope: CoroutineScope,
) = subscribe(provider, scope)

fun <S : MVIState, I : MVIIntent, A : MVIAction> MVISubscriber<S, A>.subscribe(
    provider: MVIProvider<S, I, A>,
    scope: CoroutineScope
) = provider.subscribe(scope, ::consume, ::render)

/**
 * Subscribe to the store.
 * This function is __not__ lifecycle-aware and just uses provided scope for flow collection.
 */
inline fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIProvider<S, I, A>.subscribe(
    scope: CoroutineScope,
    crossinline consume: (A) -> Unit,
    crossinline render: (S) -> Unit,
) = scope.launch {

    launch {
        actions.collect { consume(it) }
    }

    launch {
        states.collect { render(it) }
    }
}

/**
 * Execute [block] if current state is [T] else just return [currentState].
 */
inline fun <reified T : S, reified S : MVIState> MVIProvider<S, *, *>.withState(
    block: T.() -> S
): S = (currentState as? T)?.let(block) ?: currentState

/**
 * Launch a new coroutine, that will attempt to set a new state that resulted in [block] execution.
 * In case of exception being thrown, [recover] will be executed in an attempt to recover from it.
 */
fun <S : MVIState> MVIStore<S, *, *>.launchForState(
    scope: CoroutineScope,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    recover: suspend CoroutineScope.(Exception) -> S = { throw it },
    block: suspend CoroutineScope.() -> S,
) = scope.launch(context, start) {
    set(
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            recover(e)
        }
    )
}
