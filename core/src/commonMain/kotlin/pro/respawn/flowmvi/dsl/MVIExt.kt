package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIProvider
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.MVIStore
import pro.respawn.flowmvi.MVISubscriber
import pro.respawn.flowmvi.MVIView
import pro.respawn.flowmvi.Recover
import pro.respawn.flowmvi.Reduce
import pro.respawn.flowmvi.base.Reducer
import pro.respawn.flowmvi.ReducerScope
import pro.respawn.flowmvi.store.Store
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmName

/**
 * Subscribe to the store using provided scope.
 * This function is __not__ lifecycle-aware and just uses provided scope for flow collection.
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIView<S, I, A>.subscribe(
    scope: CoroutineScope,
): Job = subscribe(store, scope)

/**
 * Subscribe to the store using provided scope.
 * This function is __not__ lifecycle-aware and just uses provided scope for flow collection.
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> MVISubscriber<S, A>.subscribe(
    provider: MVIProvider<S, I, A>,
    scope: CoroutineScope
): Job = provider.subscribe(scope, ::consume, ::render)

/**
 * Subscribe to the store using provided scope.
 * This function is __not__ lifecycle-aware and just uses provided scope for flow collection.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIProvider<S, I, A>.subscribe(
    scope: CoroutineScope,
    crossinline consume: (action: A) -> Unit,
    crossinline render: (state: S) -> Unit,
): Job = scope.launch {
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
public inline fun <T> Flow<T>.catchExceptions(
    crossinline block: suspend FlowCollector<T>.(Exception) -> Unit
): Flow<T> = catch { throwable -> (throwable as? Exception)?.let { block(it) } ?: throw throwable }

/**
 * Do the operation on [this] if the type of [this] is [T], and return [R], otherwise return [this]
 */
@OverloadResolutionByLambdaReturnType
@FlowMVIDSL
public inline fun <reified T, R> R.withType(@BuilderInference block: T.() -> R): R {
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
@OverloadResolutionByLambdaReturnType
@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState, R> MVIStore<S, *, *>.withState(
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
@OverloadResolutionByLambdaReturnType
@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState, R> ReducerScope<S, *, *>.withState(
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
 * @see MVIStore.updateState
 * @see [withState]
 */
@JvmName("updateStateTyped")
@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState> ReducerScope<S, *, *>.updateState(
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
 * **This function will suspend until all previous [MVIStore.updateState] invocations are finished.**
 * @see MVIStore.updateState
 * @see [withState]
 */
@JvmName("updateStateTyped")
@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState> Store<S, *>.updateState(
    @BuilderInference crossinline transform: suspend T.() -> S
): S {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return updateState { withType<T, _> { transform() } }
}

// this BS is happening because kotlin context receivers are not available yet and there's no other way to provide multiple contexts
/**
 * A property that returns a [Reduce] lambda using the given [Reducer].
 * May be needed to deal with contexts of invocation.
 */
public inline val <S : MVIState, I : MVIIntent> Reducer<S, I>.reduce: Reduce<S, I, *>
    get() = { with(this as CoroutineScope) { reduce(it) } }

/**
 * A property that returns a [Recover] lambda using the given [Reducer].
 * May be needed to deal with contexts of invocation.
 */
public inline val <S : MVIState> Reducer<S, *>.recover: Recover<S> get() = { recover(it) }

/**
 * Launch a new coroutine using given scope,
 * and use either provided [recover] block or the [MVIStore]'s recover block.
 * Exceptions thrown in the [block] or in the nested coroutines will be handled by [recover].
 * This function does not update or obtain the state, for that, use [withState] or [updateState] inside [block].
 */
@FlowMVIDSL
public fun CoroutineScope.launchRecovering(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    recover: (Exception) -> Unit = { throw it },
    block: suspend CoroutineScope.() -> Unit,
): Job = launch(context, start) {
    try {
        supervisorScope(block)
    } catch (expected: CancellationException) {
        throw expected
    } catch (expected: Exception) {
        recover(expected)
    }
}
