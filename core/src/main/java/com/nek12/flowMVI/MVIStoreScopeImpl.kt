package com.nek12.flowMVI

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlin.coroutines.CoroutineContext

internal class MVIStoreScopeImpl<S : MVIState, in I : MVIIntent, A : MVIAction>(
    private val scope: CoroutineScope,
    private val store: MVIStore<S, I, A>,
) : CoroutineScope by scope, MVIStoreScope<S, I, A> {

    override fun send(action: A) = store.send(action)

    override fun launchRecovering(
        context: CoroutineContext,
        start: CoroutineStart,
        recover: Recover<S>?,
        block: suspend CoroutineScope.() -> Unit,
    ) = store.launchRecovering(scope, context, start, recover, block)

    override suspend fun <R> withState(block: suspend S.() -> R): R = store.withState(block)

    @DelicateStoreApi
    override var state by store::state
}
