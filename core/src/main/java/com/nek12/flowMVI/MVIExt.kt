package com.nek12.flowMVI

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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
 * Catches exceptions only, rethrowing any throwables
 */
inline fun <T> Flow<T>.catchExceptions(crossinline block: suspend FlowCollector<T>.(Exception) -> Unit) =
    catch { throwable -> (throwable as? Exception)?.let { block(it) } ?: throw throwable }

inline fun <reified T, R> R.withType(@BuilderInference block: T.() -> R) = (this as? T)?.let(block) ?: this

//non-typed
suspend inline fun <S : MVIState> MVIStore<S, *, *>.updateState(
    @BuilderInference
    crossinline transform: suspend S.() -> S
): S = withState { set(transform()) }

suspend inline fun <S : MVIState> MVIStoreScope<S, *, *>.updateState(
    @BuilderInference crossinline transform: suspend S.() -> S
): S = withState { set(transform()) }

//typed
@JvmName("updateStateTyped")
suspend inline fun <reified T : S, S : MVIState> MVIStoreScope<S, *, *>.updateState(
    @BuilderInference crossinline transform: suspend T.() -> S
) = updateState { withType<T, S> { transform() } }

@JvmName("updateStateTyped")
suspend inline fun <reified T : S, S : MVIState> MVIStore<S, *, *>.updateState(
    @BuilderInference crossinline transform: suspend T.() -> S
): S = updateState { withType<T, S> { transform() } }

//typed
suspend inline fun <reified T : S, S : MVIState, R> MVIStore<S, *, *>.withState(
    @BuilderInference crossinline block: suspend T.() -> R
): R? = withState { (this as? T)?.let { it.block() } }

suspend inline fun <reified T : S, S : MVIState, R> MVIStoreScope<S, *, *>.withState(
    @BuilderInference crossinline block: suspend T.() -> R
): R? = withState { (this as? T)?.let { it.block() } }

fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyStore(
    initial: S,
    behavior: ActionShareBehavior = ActionShareBehavior.RESTRICT,
    actionBuffer: Int = DEFAULT_ACTION_BUFFER_SIZE,
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    @BuilderInference recover: Recover<S> = { throw it },
    @BuilderInference reduce: Reducer<S, I, A>,
) = lazy(mode) { MVIStore(initial, behavior, actionBuffer, recover, reduce) }

fun <S : MVIState, I : MVIIntent, A : MVIAction> launchedStore(
    scope: CoroutineScope,
    initial: S,
    behavior: ActionShareBehavior = ActionShareBehavior.RESTRICT,
    actionBuffer: Int = DEFAULT_ACTION_BUFFER_SIZE,
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    @BuilderInference recover: Recover<S> = { throw it },
    @BuilderInference reduce: Reducer<S, I, A>
) = lazy(mode) { MVIStore(initial, behavior, actionBuffer, recover, reduce).apply { launch(scope) } }
