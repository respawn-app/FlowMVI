package com.nek12.flowMVI

import com.nek12.flowMVI.ActionShareBehavior.DISTRIBUTE
import com.nek12.flowMVI.ActionShareBehavior.RESTRICT
import com.nek12.flowMVI.ActionShareBehavior.SHARE
import com.nek12.flowMVI.store.ConsumingStore
import com.nek12.flowMVI.store.DistributingStore
import com.nek12.flowMVI.store.SharedStore

const val DEFAULT_ACTION_BUFFER_SIZE = 64

@Suppress("FunctionName")
fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIStore(
    initialState: S,
    /**
     * A behavior to be applied when sharing actions
     * @see ActionShareBehavior
     */
    behavior: ActionShareBehavior = RESTRICT,
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
