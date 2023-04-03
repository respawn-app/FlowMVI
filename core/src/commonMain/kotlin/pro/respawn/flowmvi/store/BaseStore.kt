package pro.respawn.flowmvi.store

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow.SUSPEND
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.yield
import pro.respawn.flowmvi.DelicateStoreApi
import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.MVIStore
import pro.respawn.flowmvi.Recover
import pro.respawn.flowmvi.Reduce
import pro.respawn.flowmvi.ReducerScopeImpl
import pro.respawn.flowmvi.util.withReentrantLock
import kotlin.coroutines.CoroutineContext

internal abstract class BaseStore<S : MVIState, in I : MVIIntent, A : MVIAction>(
    initialState: S,
    @BuilderInference private val recover: Recover<S>,
    @BuilderInference private val reduce: Reduce<S, I, A>,
) : MVIStore<S, I, A> {

    private val _states = MutableStateFlow(initialState)
    private val isLaunched = atomic(false)
    private val intents = Channel<I>(Channel.UNLIMITED, SUSPEND)
    private val stateMutex = Mutex()

    @DelicateStoreApi
    override val state by _states::value
    override val states: StateFlow<S> = _states.asStateFlow()

    override fun start(scope: CoroutineScope): Job {
        require(!isLaunched.getAndSet(true)) { "Store is already started" }

        return scope.launch {
            val childScope = ReducerScopeImpl(
                scope = this + SupervisorJob(),
                store = this@BaseStore
            )
            while (isActive) {
                try {
                    childScope.reduce(intents.receive())
                } catch (expected: CancellationException) {
                    throw expected
                } catch (expected: Exception) {
                    recover(expected)
                }
                yield()
            }
        }.apply {
            invokeOnCompletion { isLaunched.getAndSet(false) }
        }
    }

    override fun send(intent: I) {
        intents.trySend(intent)
    }

    override suspend fun <R> withState(block: suspend S.() -> R): R =
        stateMutex.withReentrantLock { block(states.value) }

    override suspend fun updateState(transform: suspend S.() -> S): S =
        stateMutex.withReentrantLock { transform(_states.value).also { _states.value = it } }

    override fun CoroutineScope.launchRecovering(
        context: CoroutineContext,
        start: CoroutineStart,
        recover: Recover<S>?,
        block: suspend CoroutineScope.() -> Unit,
    ): Job = launch(context, start) {
        try {
            supervisorScope(block)
        } catch (expected: CancellationException) {
            throw expected
        } catch (expected: Exception) {
            _states.update { (recover ?: this@BaseStore.recover).invoke(expected) }
        }
    }
}
