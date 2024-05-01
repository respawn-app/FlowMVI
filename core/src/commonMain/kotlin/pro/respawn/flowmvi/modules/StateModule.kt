@file:Suppress("OVERRIDE_BY_INLINE")

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
): StateModule<S> = StateModuleImpl(initial, if (atomic) Mutex() else null)

internal interface StateModule<S : MVIState> : StateReceiver<S>, StateProvider<S>

private class StateModuleImpl<S : MVIState>(
    initial: S,
    private val mutex: Mutex?,
) : StateModule<S> {

    @Suppress("VariableNaming")
    private val _states = MutableStateFlow(initial)
    override val states: StateFlow<S> = _states.asStateFlow()

    @DelicateStoreApi
    override val state: S by states::value

    override inline fun useState(block: S.() -> S) = _states.update(block)

    override suspend inline fun withState(
        crossinline block: suspend S.() -> Unit
    ) = mutex.withReentrantLock { block(states.value) }

    override suspend inline fun updateState(
        crossinline transform: suspend S.() -> S
    ) = mutex.withReentrantLock { _states.update { transform(it) } }
}
