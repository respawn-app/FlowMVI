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

internal interface StateModule<S : MVIState> : StateReceiver<S>, StateProvider<S>

internal fun <S : MVIState> stateModule(initial: S): StateModule<S> = StateModuleImpl(initial)

private class StateModuleImpl<S : MVIState>(initial: S) : StateModule<S> {

    private val _states = MutableStateFlow(initial)
    private val stateMutex = Mutex()

    @DelicateStoreApi
    override val state by _states::value

    @DelicateStoreApi
    override fun useState(block: S.() -> S) = _states.update(block)

    override val states: StateFlow<S> = _states.asStateFlow()

    override suspend fun withState(block: suspend S.() -> Unit) =
        stateMutex.withReentrantLock { block(states.value) }

    override suspend fun updateState(transform: suspend S.() -> S) =
        stateMutex.withReentrantLock { _states.update { transform(it) } }
}
