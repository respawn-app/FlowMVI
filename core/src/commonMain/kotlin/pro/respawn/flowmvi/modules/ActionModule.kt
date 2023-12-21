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

internal fun <A : MVIAction> actionModule(
    behavior: ActionShareBehavior,
): ActionModule<A> = when (behavior) {
    is ActionShareBehavior.Distribute -> DistributingModule(behavior.buffer, behavior.overflow)
    is ActionShareBehavior.Restrict -> ConsumingModule(behavior.buffer, behavior.overflow)
    is ActionShareBehavior.Share -> SharedModule(behavior.replay, behavior.buffer, behavior.overflow)
    is ActionShareBehavior.Disabled -> ThrowingModule()
}

internal interface ActionModule<A : MVIAction> : ActionProvider<A>, ActionReceiver<A>

internal abstract class ChannelActionModule<A : MVIAction>(
    bufferSize: Int,
    overflow: BufferOverflow,
) : ActionModule<A> {

    protected val delegate = Channel<A>(bufferSize, overflow)

    @DelicateStoreApi
    override fun send(action: A) {
        delegate.trySend(action)
    }

    override suspend fun action(action: A) = delegate.send(action)
}

internal class DistributingModule<A : MVIAction>(
    bufferSize: Int,
    overflow: BufferOverflow,
) : ChannelActionModule<A>(bufferSize, overflow) {

    override val actions = delegate.receiveAsFlow()
}

internal class ConsumingModule<A : MVIAction>(
    bufferSize: Int,
    overflow: BufferOverflow,
) : ChannelActionModule<A>(bufferSize, overflow) {

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

    override val actions get() = error(ActionsDisabledMessage)

    @DelicateStoreApi
    override fun send(action: A) = error(ActionsDisabledMessage)
    override suspend fun action(action: A) = error(ActionsDisabledMessage)

    private companion object {

        private const val ActionsDisabledMessage = "Actions are disabled for this store"
    }
}
