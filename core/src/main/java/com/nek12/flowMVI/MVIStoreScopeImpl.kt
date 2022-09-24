package com.nek12.flowMVI

import kotlinx.coroutines.CoroutineScope

class MVIStoreScopeImpl<S: MVIState, in I: MVIIntent, A: MVIAction>(
    scope: CoroutineScope,
    private val store: MVIStore<S, I, A>,
): CoroutineScope by scope, MVIProvider<S, I, A> by store, MVIStoreScope<S, I, A> {

    /**
     * @see MVIStore.send
     */
    override fun send(action: A) = store.send(action)

    /**
     * @see MVIStore.set
     */
    override fun set(state: S) = store.set(state)
}
