package pro.respawn.flowmvi.modules

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StateProvider
import pro.respawn.flowmvi.api.StateReceiver
import pro.respawn.flowmvi.util.withReentrantLock

internal fun <S : MVIState> stateModule(
    initial: S,
    atomic: Boolean,
): StateModule<S> = if (atomic) AtomicStateModule(initial) else DefaultStateModule(initial)

internal interface StateModule<S : MVIState> : StateReceiver<S>, StateProvider<S>

private abstract class AbstractStateModule<S : MVIState>(initial: S) : StateModule<S> {

    @Suppress("PropertyName")
    protected val _states = MutableStateFlow(initial)
    final override val states: StateFlow<S> = _states.asStateFlow()

    @DelicateStoreApi
    final override val state by _states::value

    final override fun useState(block: S.() -> S) = _states.update(block)
}

private class AtomicStateModule<S : MVIState>(initial: S) : AbstractStateModule<S>(initial) {

    private val stateMutex = Mutex()

    override suspend fun withState(
        block: suspend S.() -> Unit
    ) = stateMutex.withReentrantLock { block(states.value) }

    override suspend fun updateState(
        transform: suspend S.() -> S
    ) = stateMutex.withReentrantLock { _states.update { transform(it) } }
}

private class DefaultStateModule<S : MVIState>(initial: S) : AbstractStateModule<S>(initial) {

    override suspend fun updateState(transform: suspend S.() -> S) = _states.update { transform(it) }
    override suspend fun withState(block: suspend S.() -> Unit) = _states.value.block()
}
