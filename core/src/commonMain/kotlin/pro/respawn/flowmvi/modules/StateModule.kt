@file:OptIn(InternalFlowMVIAPI::class)

package pro.respawn.flowmvi.modules

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.ImmediateStateReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.updateStateImmediate
import pro.respawn.flowmvi.util.withReentrantLock

internal class StateModule<S : MVIState, I : MVIIntent, A : MVIAction>(
    initial: S,
    atomic: Boolean,
    private val transform: (suspend PipelineContext<S, I, A>.(old: S, new: S) -> S?)?
) : ImmediateStateReceiver<S> {

    @Suppress("VariableNaming")
    private val _states = MutableStateFlow(initial)
    override val states: StateFlow<S> = _states.asStateFlow()
    private val mutex = if (atomic) Mutex() else null

    override fun compareAndSet(expect: S, new: S) = _states.compareAndSet(expect, new)

    suspend inline fun withState(
        crossinline block: suspend S.() -> Unit
    ) = mutex.withReentrantLock { block(states.value) }

    suspend inline fun PipelineContext<S, I, A>.useState(
        crossinline transform: suspend S.() -> S
    ) = mutex.withReentrantLock block@{
        val delegate = this@StateModule.transform ?: return@block updateStateImmediate { transform() }
        updateStateImmediate { delegate(this, transform()) ?: this }
    }
}
