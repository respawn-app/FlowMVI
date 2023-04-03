package pro.respawn.flowmvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable.start
import pro.respawn.flowmvi.store.ConsumingStore
import pro.respawn.flowmvi.store.DistributingStore
import pro.respawn.flowmvi.store.SharedStore

/**
 * A builder function of [MVIStore]
 */
@Suppress("FunctionName")
public fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIStore(
    initialState: S,
    /**
     * A behavior to be applied when sharing actions
     * @see ActionShareBehavior
     */
    behavior: ActionShareBehavior = ActionShareBehavior.Distribute(),
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
): MVIStore<S, I, A> = when (behavior) {
    is ActionShareBehavior.Share -> SharedStore(initialState, behavior.replay, behavior.buffer, recover, reduce)
    is ActionShareBehavior.Distribute -> DistributingStore(initialState, behavior.buffer, recover, reduce)
    is ActionShareBehavior.Restrict -> ConsumingStore(initialState, behavior.buffer, recover, reduce)
}

/**
 * A builder function of [MVIStore] that creates  the store lazily. This function does NOT launch the store.
 * @see MVIStore
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyStore(
    initial: S,
    behavior: ActionShareBehavior = ActionShareBehavior.Distribute(),
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    @BuilderInference recover: Recover<S> = { throw it },
    @BuilderInference reduce: Reduce<S, I, A>,
): Lazy<MVIStore<S, I, A>> = lazy(mode) { MVIStore(initial, behavior, recover, reduce) }

/**
 * A builder function of [MVIStore] that creates, and then launches the store lazily.
 * @see MVIStore
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> launchedStore(
    scope: CoroutineScope,
    initial: S,
    behavior: ActionShareBehavior = ActionShareBehavior.Distribute(),
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    @BuilderInference recover: Recover<S> = { throw it },
    @BuilderInference reduce: Reduce<S, I, A>
): Lazy<MVIStore<S, I, A>> = lazy(mode) { MVIStore(initial, behavior, recover, reduce).apply { start(scope) } }
