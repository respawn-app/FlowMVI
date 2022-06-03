package com.nek12.flowMVI

import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow

internal sealed class ChannelStore<S: MVIState, in I: MVIIntent, A: MVIAction>(
    initialState: S,
    recover: (e: Exception) -> S,
    reduce: suspend (I) -> S,
): Store<S, I, A>(initialState, recover, reduce) {

    protected val internalActions = Channel<A>(Channel.BUFFERED, DROP_OLDEST)

    override fun send(action: A) {
        internalActions.trySend(action)
    }
}

internal class DistributingStore<S: MVIState, in I: MVIIntent, A: MVIAction>(
    initialState: S,
    recover: (e: Exception) -> S,
    reduce: suspend (I) -> S,
): ChannelStore<S, I, A>(initialState, recover, reduce) {

    override val actions = internalActions.receiveAsFlow()
}

internal class ConsumingStore<S: MVIState, I: MVIIntent, A: MVIAction>(
    initialState: S,
    recover: (e: Exception) -> S,
    reduce: suspend (I) -> S,
): ChannelStore<S, I, A>(initialState, recover, reduce) {

    override val actions = internalActions.consumeAsFlow()
}
