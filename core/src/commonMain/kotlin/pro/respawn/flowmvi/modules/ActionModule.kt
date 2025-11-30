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
import pro.respawn.flowmvi.util.withMap

internal interface ActionModule<A : MVIAction> : ActionProvider<A>, ActionReceiver<A>

internal fun <A : MVIAction> actionModule(
    behavior: ActionShareBehavior,
    onUndelivered: ((action: A) -> Unit)?,
    onDispatch: ((action: A) -> A?)?,
): ActionModule<A> = when (behavior) {
    is ActionShareBehavior.Distribute -> DistributingModule(
        bufferSize = behavior.buffer,
        overflow = behavior.overflow,
        onUndelivered = onUndelivered,
        onDispatch = onDispatch
    )
    is ActionShareBehavior.Restrict -> ConsumingModule(
        bufferSize = behavior.buffer,
        overflow = behavior.overflow,
        onUndelivered = onUndelivered,
        onDispatch = onDispatch,
    )
    is ActionShareBehavior.Share -> SharedModule(
        replay = behavior.replay,
        bufferSize = behavior.buffer,
        overflow = behavior.overflow,
        onDispatch = onDispatch
    )
    is ActionShareBehavior.Disabled -> ThrowingModule()
}

internal abstract class ChannelActionModule<A : MVIAction>(
    bufferSize: Int,
    overflow: BufferOverflow,
    onUndelivered: ((action: A) -> Unit)?,
) : ActionModule<A> {

    protected val delegate = Channel(bufferSize, overflow, onUndelivered)

    @DelicateStoreApi
    override fun send(action: A) {
        delegate.trySend(action)
    }

    override suspend fun action(action: A) = delegate.send(action)
}

internal class DistributingModule<A : MVIAction>(
    bufferSize: Int,
    overflow: BufferOverflow,
    onUndelivered: ((action: A) -> Unit)?,
    onDispatch: ((action: A) -> A?)?,
) : ChannelActionModule<A>(bufferSize, overflow, onUndelivered) {

    override val actions = delegate.receiveAsFlow().withMap(onDispatch)
}

internal class ConsumingModule<A : MVIAction>(
    bufferSize: Int,
    overflow: BufferOverflow,
    onUndelivered: ((action: A) -> Unit)?,
    onDispatch: ((action: A) -> A?)?,
) : ChannelActionModule<A>(bufferSize, overflow, onUndelivered) {

    override val actions = delegate.consumeAsFlow().withMap(onDispatch)
}

internal class SharedModule<A : MVIAction>(
    replay: Int,
    bufferSize: Int,
    overflow: BufferOverflow,
    onDispatch: ((action: A) -> A?)?,
) : ActionModule<A> {

    private val _actions = MutableSharedFlow<A>(
        replay = replay,
        extraBufferCapacity = bufferSize,
        onBufferOverflow = overflow
    )

    override val actions = _actions.asSharedFlow().withMap(onDispatch)

    @DelicateStoreApi
    override fun send(action: A) {
        _actions.tryEmit(action)
    }

    override suspend fun action(action: A) = _actions.emit(action)
}

internal class ThrowingModule<A : MVIAction> : ActionModule<A> {

    override val actions get() = throw ActionsDisabledException(null)

    @DelicateStoreApi
    override fun send(action: A) = actions
    override suspend fun action(action: A) = actions
}
