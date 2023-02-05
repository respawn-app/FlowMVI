package pro.respawn.flowmvi.store

import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.Recover
import pro.respawn.flowmvi.Reducer

internal class SharedStore<S : MVIState, in I : MVIIntent, A : MVIAction>(
    initialState: S,
    replay: Int,
    actionBufferSize: Int,
    @BuilderInference recover: Recover<S>,
    @BuilderInference reduce: Reducer<S, I, A>,
) : BaseStore<S, I, A>(initialState, recover, reduce) {

    private val _actions = MutableSharedFlow<A>(
        replay = replay,
        extraBufferCapacity = actionBufferSize,
        onBufferOverflow = DROP_OLDEST
    )

    override val actions = _actions.asSharedFlow()

    override fun send(action: A) {
        _actions.tryEmit(action) // will always succeed
    }
}
