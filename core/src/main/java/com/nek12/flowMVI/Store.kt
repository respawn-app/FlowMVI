package com.nek12.flowMVI

import com.nek12.flowMVI.ActionShareBehavior.DISTRIBUTE
import com.nek12.flowMVI.ActionShareBehavior.RESTRICT
import com.nek12.flowMVI.ActionShareBehavior.SHARE
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@Suppress("FunctionName")
fun <S: MVIState, I: MVIIntent, A: MVIAction> MVIStore(
    /**
     * A scope, in which coroutines that process intents will be launched.
     */
    scope: CoroutineScope,
    initialState: S,
    /**
     * A behavior to be applied when sharing actions
     * @see ActionShareBehavior
     */
    behavior: ActionShareBehavior = RESTRICT,
    /**
     * State to emit when [reduce] throws.
     *
     *  **Default implementation rethrows the exception**
     */
    recover: (e: Exception) -> S = { throw it },
    /**
     * Reduce view's intent to a new ui state.
     * Use [send] for sending side-effects for the view to handle.
     */
    reduce: suspend (I) -> S
): MVIStore<S, I, A> = when (behavior) {
    SHARE -> SharedStore(scope, initialState, recover, reduce)
    DISTRIBUTE -> DistributingStore(scope, initialState, recover, reduce)
    RESTRICT -> ConsumingStore(scope, initialState, recover, reduce)
}

internal abstract class Store<S: MVIState, in I: MVIIntent, A: MVIAction>(
    private val scope: CoroutineScope,
    initialState: S,
    private val recover: (e: Exception) -> S,
    private val reduce: suspend (I) -> S,
): MVIStore<S, I, A> {

    private val _states = MutableStateFlow(initialState)

    override val states: StateFlow<S> = _states.asStateFlow()

    override fun set(state: S) {
        _states.tryEmit(state) //will always succeed
    }

    override fun send(intent: I) {
        scope.launch {
            set(
                try {
                    reduce(intent)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    recover(e)
                }
            )
        }
    }
}
