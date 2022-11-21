package com.nek12.flowMVI.store

import com.nek12.flowMVI.MVIAction
import com.nek12.flowMVI.MVIIntent
import com.nek12.flowMVI.MVIState
import com.nek12.flowMVI.Recover
import com.nek12.flowMVI.Reducer
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class SharedStore<S : MVIState, in I : MVIIntent, A : MVIAction>(
    initialState: S,
    actionBufferSize: Int,
    @BuilderInference recover: Recover<S>,
    @BuilderInference reduce: Reducer<S, I, A>,
) : BaseStore<S, I, A>(initialState, recover, reduce) {

    private val _actions = MutableSharedFlow<A>(
        replay = 0,
        extraBufferCapacity = actionBufferSize,
        onBufferOverflow = DROP_OLDEST
    )

    override val actions = _actions.asSharedFlow()

    override fun send(action: A) {
        _actions.tryEmit(action) // will always succeed
    }
}
