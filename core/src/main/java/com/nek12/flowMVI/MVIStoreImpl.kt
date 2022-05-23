package com.nek12.flowMVI

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

fun <S: MVIState, I: MVIIntent, A: MVIAction> MVIStore(
    scope: CoroutineScope,
    initialState: S,
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
): MVIStore<S, I, A> = MVIStoreImpl(scope, initialState, recover, reduce)

private class MVIStoreImpl<S: MVIState, in I: MVIIntent, A: MVIAction>(
    private val scope: CoroutineScope,
    initialState: S,
    private val recover: (e: Exception) -> S,
    private val reduce: suspend (I) -> S,
): MVIStore<S, I, A> {

    private val _states = MutableStateFlow(initialState)

    private val _actions = MutableSharedFlow<A>(
        replay = 0,
        extraBufferCapacity = DEFAULT_BUFFER_CAPACITY,
        onBufferOverflow = DROP_OLDEST
    )

    override val states: StateFlow<S> = _states.asStateFlow()
    override val actions: Flow<A> = _actions.asSharedFlow()

    override fun set(state: S) {
        _states.tryEmit(state) //will always succeed
    }

    override fun send(action: A) {
        _actions.tryEmit(action) //will always succeed
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

    companion object {

        private const val DEFAULT_BUFFER_CAPACITY = 64
    }
}
