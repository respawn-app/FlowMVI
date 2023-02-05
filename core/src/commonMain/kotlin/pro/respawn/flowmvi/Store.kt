package pro.respawn.flowmvi

import kotlinx.coroutines.CoroutineScope
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
     * A buffer size for actions that are left unprocessed in the store.
     * On buffer overflow, the oldest action will be dropped.
     * Intents have unlimited buffer.
     */
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
    @BuilderInference reduce: Reducer<S, I, A>,
): MVIStore<S, I, A> = when (behavior) {
    is ActionShareBehavior.Share -> SharedStore(initialState, behavior.replay, behavior.buffer, recover, reduce)
    is ActionShareBehavior.Distribute -> DistributingStore(initialState, behavior.buffer, recover, reduce)
    is ActionShareBehavior.Restrict -> ConsumingStore(initialState, behavior.buffer, recover, reduce)
}

/**
 * A builder function of [MVIStore] that creates  the store lazily. This function does NOT launch the store.
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyStore(
    initial: S,
    behavior: ActionShareBehavior = ActionShareBehavior.Distribute(),
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    @BuilderInference recover: Recover<S> = { throw it },
    @BuilderInference reduce: Reducer<S, I, A>,
): Lazy<MVIStore<S, I, A>> = lazy(mode) { MVIStore(initial, behavior, recover, reduce) }

/**
 * A builder function of [MVIStore] that creates, and then launches the store lazily.
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> launchedStore(
    scope: CoroutineScope,
    initial: S,
    behavior: ActionShareBehavior = ActionShareBehavior.Distribute(),
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    @BuilderInference recover: Recover<S> = { throw it },
    @BuilderInference reduce: Reducer<S, I, A>
): Lazy<MVIStore<S, I, A>> = lazy(mode) { MVIStore(initial, behavior, recover, reduce).apply { start(scope) } }
