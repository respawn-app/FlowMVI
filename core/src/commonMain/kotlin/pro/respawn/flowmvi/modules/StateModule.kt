@file:Suppress("OVERRIDE_BY_INLINE")
@file:OptIn(InternalFlowMVIAPI::class)

package pro.respawn.flowmvi.modules

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StateProvider
import pro.respawn.flowmvi.api.StateReceiver
import pro.respawn.flowmvi.util.withReentrantLock

internal fun <S : MVIState> stateModule(
    initial: S,
    atomic: Boolean,
): StateModule<S> = if (atomic) AtomicStateModule(initial) else DefaultStateModule(initial)

internal interface StateModule<S : MVIState> : StateReceiver<S>, StateProvider<S> {

    @DelicateStoreApi
    @InternalFlowMVIAPI
    override val state: S
}

private abstract class AbstractStateModule<S : MVIState>(initial: S) : StateModule<S> {

    final override val states = MutableStateFlow(initial)

    @DelicateStoreApi
    final override val state by states::value

    final override inline fun useState(block: S.() -> S) = states.update(block)
}

private class AtomicStateModule<S : MVIState>(initial: S) : AbstractStateModule<S>(initial) {

    private val stateMutex = Mutex()

    override suspend inline fun withState(
        crossinline block: suspend S.() -> Unit
    ) = stateMutex.withReentrantLock { block(states.value) }

    override suspend inline fun updateState(
        crossinline transform: suspend S.() -> S
    ) = stateMutex.withReentrantLock { states.update { transform(it) } }
}

private class DefaultStateModule<S : MVIState>(initial: S) : AbstractStateModule<S>(initial) {

    override suspend inline fun updateState(crossinline transform: suspend S.() -> S) = states.update { transform(it) }
    override suspend inline fun withState(crossinline block: suspend S.() -> Unit) = states.value.block()
}
