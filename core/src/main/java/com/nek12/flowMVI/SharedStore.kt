package com.nek12.flowMVI

import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class SharedStore<S: MVIState, in I: MVIIntent, A: MVIAction>(
    initialState: S,
    recover: MVIStore<S, I, A>.(e: Exception) -> S,
    reduce: suspend MVIStore<S, I, A>.(I) -> S,
): Store<S, I, A>(initialState, recover, reduce) {

    private val _actions = MutableSharedFlow<A>(
        replay = 0,
        extraBufferCapacity = DEFAULT_BUFFER_CAPACITY,
        onBufferOverflow = DROP_OLDEST
    )

    override val actions = _actions.asSharedFlow()

    override fun send(action: A) {
        _actions.tryEmit(action) //will always succeed
    }

    companion object {
        private const val DEFAULT_BUFFER_CAPACITY = 64
    }
}
