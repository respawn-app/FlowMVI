package pro.respawn.flowmvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

internal class ReducerScopeImpl<S : MVIState, in I : MVIIntent, A : MVIAction>(
    scope: CoroutineScope,
    private val store: MVIStore<S, I, A>,
) : ReducerScope<S, I, A>, CoroutineScope by scope {

    override fun send(action: A) = store.send(action)

    override fun launchRecovering(
        context: CoroutineContext,
        start: CoroutineStart,
        recover: Recover<S>?,
        block: suspend CoroutineScope.() -> Unit,
    ): Job = launchRecovering(context, start, recover, block)

    override suspend fun <R> withState(block: suspend S.() -> R): R = store.withState(block)

    override suspend fun updateState(transform: suspend S.() -> S) = store.updateState(transform)

    @DelicateStoreApi
    override val state by store::state
}
