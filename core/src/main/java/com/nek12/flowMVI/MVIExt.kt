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
 * Catches exceptions only, rethrowing any throwables
 */
inline fun <T> Flow<T>.catchExceptions(crossinline block: suspend FlowCollector<T>.(Exception) -> Unit) =
    catch { throwable -> (throwable as? Exception)?.let { block(it) } ?: throw throwable }

/**
 * Do the operation on [this] if the type of [this] is [T], and return [R], otherwise return [this]
 */
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
 * @see MVIStore.withState
 */
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
 * @see MVIStore.withState
 */
suspend inline fun <reified T : S, S : MVIState, R> MVIStoreScope<S, *, *>.withState(
    @BuilderInference crossinline block: suspend T.() -> R
): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return withState { (this as? T)?.let { it.block() } }
}

// non-typed
/**
 * Obtain the current [MVIStore.state] and update it with the result of [transform].
 *
 * **This function will suspend until all previous [MVIStore.withState] invocations are finished.**
 *
 * If you want to operate on a state of particular subtype, use the typed version of this function.
 * @see MVIStore.updateState
 * @see [withState]
 */
@OptIn(DelicateStoreApi::class)
suspend inline fun <S : MVIState> MVIStore<S, *, *>.updateState(
    @BuilderInference
    crossinline transform: suspend S.() -> S
): S {
    contract {
        callsInPlace(transform, InvocationKind.EXACTLY_ONCE)
    }
    return withState { state = transform(); state }
}

/**
 * Obtain the current [MVIStore.state] and update it with the result of [transform].
 *
 * **This function will suspend until all previous [MVIStore.withState] invocations are finished.**
 *
 *  * If you want to operate on a state of particular subtype, use the typed version of this function.
 * @see MVIStore.updateState
 * @see [withState]
 */
@OptIn(DelicateStoreApi::class)
suspend inline fun <S : MVIState> MVIStoreScope<S, *, *>.updateState(
    @BuilderInference crossinline transform: suspend S.() -> S
): S {
    contract {
        callsInPlace(transform, InvocationKind.EXACTLY_ONCE)
    }
    return withState { state = transform(); state }
}

// typed
/**
 * Obtain the current [MVIStore.state] and update it with
 * the result of [transform] if it is of type [T], otherwise do nothing.
 *
 * **This function will suspend until all previous [MVIStore.withState] invocations are finished.**
 * @see MVIStore.updateState
 * @see [withState]
 */
@JvmName("updateStateTyped")
suspend inline fun <reified T : S, S : MVIState> MVIStoreScope<S, *, *>.updateState(
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
