@file:OptIn(ExperimentalContracts::class)

package com.nek12.flowMVI

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Subscribe to the store using provided scope.
 * This function is __not__ lifecycle-aware and just uses provided scope for flow collection.
 */
fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIView<S, I, A>.subscribe(
    scope: CoroutineScope,
) = subscribe(provider, scope)

/**
 * Subscribe to the store using provided scope.
 * This function is __not__ lifecycle-aware and just uses provided scope for flow collection.
 */
fun <S : MVIState, I : MVIIntent, A : MVIAction> MVISubscriber<S, A>.subscribe(
    provider: MVIProvider<S, I, A>,
    scope: CoroutineScope
) = provider.subscribe(scope, ::consume, ::render)

/**
 * Subscribe to the store using provided scope.
 * This function is __not__ lifecycle-aware and just uses provided scope for flow collection.
 */
inline fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIProvider<S, I, A>.subscribe(
    scope: CoroutineScope,
    crossinline consume: (action: A) -> Unit,
    crossinline render: (state: S) -> Unit,
) = scope.launch {
    launch {
        actions.collect { consume(it) }
    }

    launch {
        states.collect { render(it) }
    }
}

/**
 * Catches exceptions only, rethrowing any throwables
 */
inline fun <T> Flow<T>.catchExceptions(crossinline block: suspend FlowCollector<T>.(Exception) -> Unit) =
    catch { throwable -> (throwable as? Exception)?.let { block(it) } ?: throw throwable }

/**
 * Do the operation on [this] if the type of [this] is [T], and return [R], otherwise return [this]
 */
@OverloadResolutionByLambdaReturnType
inline fun <reified T, R> R.withType(@BuilderInference block: T.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return (this as? T)?.let(block) ?: this
}

/**
 * Run [block] if current [MVIStore.state] is of type [T], otherwise do nothing.
 *
 * **This function will suspend until all previous [MVIStore.withState] invocations are finished.**
 * **This function is not reentrant.**
 * @see MVIStore.withState
 */
@OverloadResolutionByLambdaReturnType
suspend inline fun <reified T : S, S : MVIState, R> MVIStore<S, *, *>.withState(
    @BuilderInference crossinline block: suspend T.() -> R
): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return withState { (this as? T)?.let { it.block() } }
}

/**
 * Run [block] if current [MVIStore.state] is of type [T], otherwise do nothing.
 *
 * **This function will suspend until all previous [MVIStore.withState] invocations are finished.**
 * **This function is not reentrant.**
 * @see MVIStore.withState
 */
@OverloadResolutionByLambdaReturnType
suspend inline fun <reified T : S, S : MVIState, R> ReducerScope<S, *, *>.withState(
    @BuilderInference crossinline block: suspend T.() -> R
): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return withState { (this as? T)?.let { it.block() } }
}

/**
 * Obtain the current [MVIStore.state] and update it with
 * the result of [transform] if it is of type [T], otherwise do nothing.
 *
 * **This function will suspend until all previous [MVIStore.withState] invocations are finished.**
 * **This function is not reentrant.**
 * @see MVIStore.updateState
 * @see [withState]
 */
@JvmName("updateStateTyped")
suspend inline fun <reified T : S, S : MVIState> ReducerScope<S, *, *>.updateState(
    @BuilderInference crossinline transform: suspend T.() -> S
): S {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return updateState { withType<T, _> { transform() } }
}

/**
 * Obtain the current [MVIStore.state] and update it with
 * the result of [transform] if it is of type [T], otherwise do nothing.
 *
 * **This function will suspend until all previous [MVIStore.withState] invocations are finished.**
 * **This function is not reentrant.**
 * @see MVIStore.updateState
 * @see [withState]
 */
@JvmName("updateStateTyped")
suspend inline fun <reified T : S, S : MVIState> MVIStore<S, *, *>.updateState(
    @BuilderInference crossinline transform: suspend T.() -> S
): S {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return updateState { withType<T, _> { transform() } }
}
