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
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.exceptions.ActionsDisabledException

internal interface ActionModule<A : MVIAction> : ActionProvider<A>, ActionReceiver<A>

internal fun <A : MVIAction> actionModule(
    behavior: ActionShareBehavior,
    onUndeliveredAction: ((action: A) -> Unit)?,
): ActionModule<A> = when (behavior) {
    is ActionShareBehavior.Distribute -> DistributingModule(
        bufferSize = behavior.buffer,
        overflow = behavior.overflow,
        onUndeliveredAction,
    )
    is ActionShareBehavior.Restrict -> ConsumingModule(
        behavior.buffer,
        behavior.overflow,
        onUndeliveredAction,
    )
    is ActionShareBehavior.Share -> SharedModule(behavior.replay, behavior.buffer, behavior.overflow)
    is ActionShareBehavior.Disabled -> ThrowingModule()
}

internal abstract class ChannelActionModule<A : MVIAction>(
    bufferSize: Int,
    overflow: BufferOverflow,
    onUndeliveredAction: ((action: A) -> Unit)?,
) : ActionModule<A> {

    protected val delegate = Channel(bufferSize, overflow, onUndeliveredAction)

    @DelicateStoreApi
    override fun send(action: A) {
        delegate.trySend(action)
    }

    override suspend fun action(action: A) = delegate.send(action)
}

internal class DistributingModule<A : MVIAction>(
    bufferSize: Int,
    overflow: BufferOverflow,
    onUndeliveredAction: ((action: A) -> Unit)?,
) : ChannelActionModule<A>(bufferSize, overflow, onUndeliveredAction) {

    override val actions = delegate.receiveAsFlow()
}

internal class ConsumingModule<A : MVIAction>(
    bufferSize: Int,
    overflow: BufferOverflow,
    onUndeliveredAction: ((action: A) -> Unit)?,
) : ChannelActionModule<A>(bufferSize, overflow, onUndeliveredAction) {

    override val actions = delegate.consumeAsFlow()
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

    @DelicateStoreApi
    override fun send(action: A) {
        _actions.tryEmit(action)
    }

    override suspend fun action(action: A) = _actions.emit(action)
}

internal class ThrowingModule<A : MVIAction> : ActionModule<A> {

    override val actions get() = throw ActionsDisabledException()

    @DelicateStoreApi
    override fun send(action: A) = actions
    override suspend fun action(action: A) = actions
}
