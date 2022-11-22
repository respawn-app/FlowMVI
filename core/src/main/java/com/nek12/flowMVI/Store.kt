package com.nek12.flowMVI

import com.nek12.flowMVI.ActionShareBehavior.DISTRIBUTE
import com.nek12.flowMVI.ActionShareBehavior.RESTRICT
import com.nek12.flowMVI.ActionShareBehavior.SHARE
import com.nek12.flowMVI.store.ConsumingStore
import com.nek12.flowMVI.store.DistributingStore
import com.nek12.flowMVI.store.SharedStore
import kotlinx.coroutines.CoroutineScope

const val DEFAULT_ACTION_BUFFER_SIZE = 64

/**
 * A builder function of [MVIStore]
 */
@Suppress("FunctionName")
fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIStore(
    initialState: S,
    /**
     * A behavior to be applied when sharing actions
     * @see ActionShareBehavior
     */
    behavior: ActionShareBehavior = DISTRIBUTE,
    /**
     * A buffer size for actions that are left unprocessed in the store.
     * On buffer overflow, the oldest action will be dropped.
     * Intents have unlimited buffer.
     */
    actionBuffer: Int = DEFAULT_ACTION_BUFFER_SIZE,
    /**
     * State to emit when [reduce] throws.
     *
     *  **Default implementation rethrows the exception**
     */
    @BuilderInference recover: Recover<S> = { throw it },
    /**
     * Reduce view's intent to a new ui state.
     * Use [MVIStore.send] for sending side-effects for the view to handle.
     * Coroutines launched inside [reduce] can fail independently of each other.
     */
    @BuilderInference reduce: Reducer<S, I, A>,
): MVIStore<S, I, A> = when (behavior) {
    SHARE -> SharedStore(initialState, actionBuffer, recover, reduce)
    DISTRIBUTE -> DistributingStore(initialState, actionBuffer, recover, reduce)
    RESTRICT -> ConsumingStore(initialState, actionBuffer, recover, reduce)
}

/**
 * A builder function of [MVIStore] that creates  the store lazily. This function does NOT launch the store.
 */
fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyStore(
    initial: S,
    behavior: ActionShareBehavior = RESTRICT,
    actionBuffer: Int = DEFAULT_ACTION_BUFFER_SIZE,
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    @BuilderInference recover: Recover<S> = { throw it },
    @BuilderInference reduce: Reducer<S, I, A>,
) = lazy(mode) { MVIStore(initial, behavior, actionBuffer, recover, reduce) }

/**
 * A builder function of [MVIStore] that creates, and then launches the store lazily.
 */
fun <S : MVIState, I : MVIIntent, A : MVIAction> launchedStore(
    scope: CoroutineScope,
    initial: S,
    behavior: ActionShareBehavior = RESTRICT,
    actionBuffer: Int = DEFAULT_ACTION_BUFFER_SIZE,
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    @BuilderInference recover: Recover<S> = { throw it },
    @BuilderInference reduce: Reducer<S, I, A>
) = lazy(mode) { MVIStore(initial, behavior, actionBuffer, recover, reduce).apply { start(scope) } }
