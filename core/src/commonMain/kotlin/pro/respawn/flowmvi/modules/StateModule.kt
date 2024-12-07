@file:OptIn(InternalFlowMVIAPI::class)

package pro.respawn.flowmvi.modules

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.ImmediateStateReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StateStrategy
import pro.respawn.flowmvi.api.StateStrategy.Atomic
import pro.respawn.flowmvi.api.StateStrategy.Immediate
import pro.respawn.flowmvi.dsl.updateStateImmediate
import pro.respawn.flowmvi.exceptions.RecursiveStateTransactionException
import pro.respawn.flowmvi.util.ReentrantMutexContextElement
import pro.respawn.flowmvi.util.ReentrantMutexContextKey
import pro.respawn.flowmvi.util.withReentrantLock

internal class StateModule<S : MVIState, I : MVIIntent, A : MVIAction>(
    initial: S,
    strategy: StateStrategy,
    private val debuggable: Boolean,
    private val chain: (suspend PipelineContext<S, I, A>.(old: S, new: S) -> S?)?
) : ImmediateStateReceiver<S> {

    @Suppress("VariableNaming")
    private val _states = MutableStateFlow(initial)
    override val states: StateFlow<S> = _states.asStateFlow()
    private val reentrant = strategy is Atomic && strategy.reentrant
    private val mutexElement = when (strategy) {
        is Immediate -> null
        is Atomic -> Mutex().let(::ReentrantMutexContextKey).let(::ReentrantMutexContextElement)
    }

    private suspend inline fun withLock(crossinline block: suspend () -> Unit) = when {
        mutexElement == null -> block()
        reentrant -> mutexElement.withReentrantLock(block)
        !debuggable -> mutexElement.key.mutex.withLock { block() }
        else -> {
            try {
                mutexElement.key.mutex.lock(this)
            } catch (e: IllegalStateException) {
                throw RecursiveStateTransactionException(e)
            }
            try {
                block()
            } finally {
                mutexElement.key.mutex.unlock(this)
            }
        }
    }

    override fun compareAndSet(expect: S, new: S) = _states.compareAndSet(expect, new)

    suspend inline fun withState(
        crossinline block: suspend S.() -> Unit
    ) = withLock { block(states.value) }

    suspend inline fun PipelineContext<S, I, A>.useState(
        crossinline transform: suspend S.() -> S
    ) = withLock {
        val chain = chain ?: return@withLock updateStateImmediate { transform() }
        updateStateImmediate { chain(this, transform()) ?: this }
    }
}
