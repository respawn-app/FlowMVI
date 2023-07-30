@file:Suppress("DEPRECATION")

package pro.respawn.flowmvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MutableStore
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.subscribe
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.util.withType
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

/**
 * Subscribe to the store using provided scope.
 * This function is __not__ lifecycle-aware and just uses provided scope for flow collection.
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIView<S, I, A>.subscribe(
    scope: CoroutineScope,
): Job = with(scope) { subscribe(provider, ::consume, ::render) }

/**
 * Subscribe to the store using provided scope.
 * This function is __not__ lifecycle-aware and just uses provided scope for flow collection.
 */
@Deprecated(
    "Use the new subscribe dsl",
    ReplaceWith(
        "subscribe",
        "pro.respawn.flowmvi.dsl.subscribe"
    )
)
public fun <S : MVIState, I : MVIIntent, A : MVIAction> MVISubscriber<S, A>.subscribe(
    provider: MVIProvider<S, I, A>,
    scope: CoroutineScope
): Job = provider.subscribe(scope, ::consume, ::render)

/**
 * Subscribe to the store using provided scope.
 * This function is __not__ lifecycle-aware and just uses provided scope for flow collection.
 */
@FlowMVIDSL
@Deprecated(
    "Use the new subscribe dsl",
    ReplaceWith(
        "subscribe",
        "pro.respawn.flowmvi.dsl.subscribe"
    )
)
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
 * Run [block] if current [MVIStore.state] is of type [T], otherwise do nothing.
 *
 * **This function will suspend until all previous [MVIStore.withState] invocations are finished.**
 * @see MVIStore.withState
 */
@OverloadResolutionByLambdaReturnType
@FlowMVIDSL
@Deprecated(
    "Use StateReceiver.withState",
    ReplaceWith(
        "updateState",
        "pro.respawn.flowmvi.dsl.withState"
    )
)
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
@Deprecated(
    "Use StateReceiver.withState",
    ReplaceWith(
        "updateState",
        "pro.respawn.flowmvi.dsl.withState"
    )
)
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
@Deprecated(
    "Use StateReceiver.updateState",
    ReplaceWith(
        "updateState",
        "pro.respawn.flowmvi.dsl.updateState"
    )
)
public suspend inline fun <reified T : S, S : MVIState> ReducerScope<S, *, *>.updateState(
    @BuilderInference crossinline transform: suspend T.() -> S
) {
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
@Deprecated(
    "Use StateReceiver.updateState",
    ReplaceWith(
        "this.updateState",
        "pro.respawn.flowmvi.dsl.updateState"
    )
)
public suspend inline fun <reified T : S, S : MVIState> MVIStore<S, *, *>.updateState(
    @BuilderInference crossinline transform: suspend T.() -> S
) {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return updateState { withType<T, _> { transform() } }
}

/**
 * A property that returns a [Reduce] lambda using the given [Reducer].
 * May be needed to deal with contexts of invocation.
 */
@Deprecated(
    "Not needed anymore, use store builders",
    ReplaceWith("{ with(this as CoroutineScope) { reduce(it) } }", "kotlinx.coroutines.CoroutineScope")
)
public inline val <S : MVIState, I : MVIIntent> Reducer<S, I>.reduce: Reduce<S, I, *>
    get() = { with(this as CoroutineScope) { reduce(it) } }

/**
 * A property that returns a [Recover] lambda using the given [Reducer].
 * May be needed to deal with contexts of invocation.
 */
@Deprecated("Not needed anymore, use store builders", ReplaceWith("{ recover(it) }"))
public inline val <S : MVIState> Reducer<S, *>.recover: Recover<S> get() = { recover(it) }

/**
 * A builder function of [MVIStore]
 */
@Suppress("FunctionName", "TrimMultilineRawString")
@Deprecated(
    "Use store builders",
    ReplaceWith(
        """
    store(initial) {
        actionShareBehavior = behavior
        reduce(reduce)
        recover {
            updateState { recover(it) }
            null
        }
    }
"""
    )
)
public fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIStore(
    initial: S,
    /**
     * A behavior to be applied when sharing actions
     * @see ActionShareBehavior
     */
    behavior: ActionShareBehavior = pro.respawn.flowmvi.api.ActionShareBehavior.Distribute(),
    /**
     * State to emit when [reduce] throws.
     *
     *  **Default implementation rethrows the exception**
     *  **The body of this block may be evaluated multiple times in case of concurrent state updates**
     */
    @BuilderInference recover: Recover<S> = { throw it },
    /**
     * Reduce view's intent to a new ui state.
     * Use [MVIStore.send] for sending side-effects for the view to handle.
     * Coroutines launched inside [reduce] can fail independently of each other.
     */
    @BuilderInference reduce: Reduce<S, I, A>,
): MutableStore<S, I, A> = store<S, I, A> {
    actionShareBehavior = behavior
    reduce(reduce = reduce)
    recover {
        updateState { recover(it) }
        null
    }
    initial(initial)
}

/**
 * A builder function of [MVIStore] that creates  the store lazily. This function does **not** launch the store.
 * Call [MVIStore.start] yourself as appropriate.
 * @see MVIStore
 */
@Suppress("TrimMultilineRawString")
@Deprecated(
    "Use store builders",
    ReplaceWith(
        """
    store(initial) {
        reduce(reduce)
        recover {
            updateState { recover(it) }
            null
        }
        actionShareBehavior = behavior
}
"""
    )
)
public fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyStore(
    initial: S,
    behavior: ActionShareBehavior = pro.respawn.flowmvi.api.ActionShareBehavior.Distribute(),
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    @BuilderInference recover: Recover<S> = { throw it },
    @BuilderInference reduce: Reduce<S, I, A>,
): Lazy<Store<S, I, A>> = lazy(mode) { MVIStore(initial, behavior, recover, reduce) }

/**
 * A builder function of [MVIStore] that creates, and then launches the store lazily on first access.
 * @see MVIStore
 */
@Suppress("TrimMultilineRawString")
@Deprecated(
    "Use store builders",
    ReplaceWith(
        """
    store(initial) {
        reduce(reduce)
        recover {
            updateState { recover(it) }
            null
        }
        actionShareBehavior = behavior
}
"""
    )
)
public fun <S : MVIState, I : MVIIntent, A : MVIAction> launchedStore(
    scope: CoroutineScope,
    initial: S,
    behavior: ActionShareBehavior = pro.respawn.flowmvi.api.ActionShareBehavior.Distribute(),
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    @BuilderInference recover: Recover<S> = { throw it },
    @BuilderInference reduce: Reduce<S, I, A>
): Lazy<Store<S, I, A>> = lazy(mode) { MVIStore(initial, behavior, recover, reduce).apply { start(scope) } }
