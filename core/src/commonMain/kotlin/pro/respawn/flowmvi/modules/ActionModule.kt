package pro.respawn.flowmvi.modules

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import pro.respawn.flowmvi.api.ActionProvider
import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.MVIAction

internal interface ActionModule<A : MVIAction> : ActionProvider<A>, ActionReceiver<A>

internal abstract class ChannelActionModule<A : MVIAction>(
    bufferSize: Int,
    overflow: BufferOverflow,
) : ActionModule<A> {

    protected val _actions = Channel<A>(bufferSize, overflow)

    override suspend fun send(action: A) {
        _actions.send(action)
    }
}

internal class DistributingModule<A : MVIAction>(
    bufferSize: Int,
    overflow: BufferOverflow,
) : ChannelActionModule<A>(bufferSize, overflow) {

    override val actions = _actions.receiveAsFlow()
}

internal class ConsumingModule<A : MVIAction>(
    bufferSize: Int,
    overflow: BufferOverflow,
) : ChannelActionModule<A>(bufferSize, overflow) {

    override val actions = _actions.consumeAsFlow()
}

internal class SharedModule<A : MVIAction>(
    replay: Int,
    bufferSize: Int,
    overflow: BufferOverflow,
) : ActionModule<A> {

    private val _actions = MutableSharedFlow<A>(
        replay = replay,
        extraBufferCapacity = bufferSize,
        onBufferOverflow = overflow
    )

    override val actions = _actions.asSharedFlow()

    override suspend fun send(action: A) {
        _actions.emit(action)
    }
}

internal class ThrowingModule<A : MVIAction> : ActionModule<A> {

    override val actions get() = error(message)
    override suspend fun send(action: A) = error(message)

    private companion object {

        private const val message = "Actions are disabled for this store"
    }
}

internal fun <A : MVIAction> actionModule(
    behavior: ActionShareBehavior,
): ActionModule<A> = when (behavior) {
    is ActionShareBehavior.Distribute -> DistributingModule(behavior.buffer, behavior.overflow)
    is ActionShareBehavior.Restrict -> ConsumingModule(behavior.buffer, behavior.overflow)
    is ActionShareBehavior.Share -> SharedModule(behavior.replay, behavior.buffer, behavior.overflow)
    is ActionShareBehavior.Disabled -> ThrowingModule()
}
