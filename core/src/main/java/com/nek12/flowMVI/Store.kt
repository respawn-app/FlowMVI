package com.nek12.flowMVI

import com.nek12.flowMVI.ActionShareBehavior.DISTRIBUTE
import com.nek12.flowMVI.ActionShareBehavior.RESTRICT
import com.nek12.flowMVI.ActionShareBehavior.SHARE
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.channels.BufferOverflow.SUSPEND
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("FunctionName")
fun <S: MVIState, I: MVIIntent, A: MVIAction> MVIStore(
    initialState: S,
    /**
     * A behavior to be applied when sharing actions
     * @see ActionShareBehavior
     */
    behavior: ActionShareBehavior = DISTRIBUTE,
    /**
     * State to emit when [reduce] throws.
     *
     *  **Default implementation rethrows the exception**
     */
    @BuilderInference recover: MVIStore<S, I, A>.(e: Exception) -> S = { throw it },
    /**
     * Reduce view's intent to a new ui state.
     * Use [send] for sending side-effects for the view to handle.
     */
    @BuilderInference reduce: suspend MVIStore<S, I, A>.(I) -> S
): MVIStore<S, I, A> = when (behavior) {
    SHARE -> SharedStore(initialState, recover, reduce)
    DISTRIBUTE -> DistributingStore(initialState, recover, reduce)
    RESTRICT -> ConsumingStore(initialState, recover, reduce)
}

internal abstract class Store<S: MVIState, in I: MVIIntent, A: MVIAction>(
    initialState: S,
    @BuilderInference private val recover: MVIStore<S, I, A>.(e: Exception) -> S,
    @BuilderInference private val reduce: suspend MVIStore<S, I, A>.(I) -> S,
): MVIStore<S, I, A> {

    private val _states = MutableStateFlow(initialState)
    override val states: StateFlow<S> = _states.asStateFlow()
    private val isLaunched = AtomicBoolean(false)

    private val intents = Channel<I>(Channel.UNLIMITED, SUSPEND)

    override fun set(state: S) {
        _states.value = state
    }

    override fun launch(scope: CoroutineScope): Job {
        require(!isLaunched.getAndSet(true)) { "Store is already launched" }

        return scope.launch {
            while (this.isActive) {
                set(
                    try {
                        reduce(intents.receive())
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        recover(e)
                    }
                )
            }
        }.apply {
            invokeOnCompletion { isLaunched.set(false) }
        }
    }

    override fun send(intent: I) {
        intents.trySend(intent)
    }
}
