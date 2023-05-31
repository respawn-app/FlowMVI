package pro.respawn.flowmvi.action

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import pro.respawn.flowmvi.MVIAction

internal sealed interface ActionModule<A : MVIAction> : ActionProvider<A>, ActionReceiver<A>

internal abstract class ChannelActionModule<A : MVIAction>(
    bufferSize: Int,
    overflow: BufferOverflow = BufferOverflow.DROP_OLDEST,
) : ActionModule<A> {

    protected val _actions = Channel<A>(bufferSize, overflow)

    override fun send(action: A) {
        _actions.trySend(action)
    }
}

internal class DistributingModule<A : MVIAction>(
    bufferSize: Int,
    overflow: BufferOverflow = BufferOverflow.DROP_OLDEST,
) : ChannelActionModule<A>(bufferSize, overflow) {

    override val actions = _actions.receiveAsFlow()
}

internal class ConsumingModule<A : MVIAction>(
    bufferSize: Int,
    overflow: BufferOverflow = BufferOverflow.DROP_OLDEST,
) : ChannelActionModule<A>(bufferSize, overflow) {

    override val actions = _actions.consumeAsFlow()
}

internal class SharedStore<A : MVIAction>(
    replay: Int,
    bufferSize: Int,
    overflow: BufferOverflow = BufferOverflow.DROP_OLDEST,
) : ActionModule<A> {

    private val _actions = MutableSharedFlow<A>(
        replay = replay,
        extraBufferCapacity = bufferSize,
        onBufferOverflow = overflow
    )

    override val actions = _actions.asSharedFlow()

    override fun send(action: A) {
        _actions.tryEmit(action)
    }
}

internal fun <A : MVIAction> actionModule(
    behavior: ActionShareBehavior,
): ActionModule<A> = when (behavior) {
    is ActionShareBehavior.Distribute -> DistributingModule(behavior.buffer)
    is ActionShareBehavior.Restrict -> ConsumingModule(behavior.buffer)
    is ActionShareBehavior.Share -> SharedStore(behavior.replay, behavior.buffer)
}
