package com.nek12.flowMVI.store

import com.nek12.flowMVI.MVIAction
import com.nek12.flowMVI.MVIIntent
import com.nek12.flowMVI.MVIState
import com.nek12.flowMVI.Recover
import com.nek12.flowMVI.Reducer
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow

internal sealed class ChannelStore<S : MVIState, in I : MVIIntent, A : MVIAction>(
    initialState: S,
    actionBufferSize: Int,
    @BuilderInference recover: Recover<S>,
    @BuilderInference reduce: Reducer<S, I, A>,
) : BaseStore<S, I, A>(initialState, recover, reduce) {

    protected val internalActions = Channel<A>(actionBufferSize, DROP_OLDEST)

    override fun send(action: A) {
        internalActions.trySend(action)
    }
}

internal class DistributingStore<S : MVIState, in I : MVIIntent, A : MVIAction>(
    initialState: S,
    actionBufferSize: Int,
    @BuilderInference recover: Recover<S>,
    @BuilderInference reduce: Reducer<S, I, A>,
) : ChannelStore<S, I, A>(initialState, actionBufferSize, recover, reduce) {

    override val actions = internalActions.receiveAsFlow()
}

internal class ConsumingStore<S : MVIState, I : MVIIntent, A : MVIAction>(
    initialState: S,
    actionBufferSize: Int,
    @BuilderInference recover: Recover<S>,
    @BuilderInference reduce: Reducer<S, I, A>,
) : ChannelStore<S, I, A>(initialState, actionBufferSize, recover, reduce) {

    override val actions = internalActions.consumeAsFlow()
}
