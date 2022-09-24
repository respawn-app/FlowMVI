package com.nek12.flowMVI.store

import com.nek12.flowMVI.MVIAction
import com.nek12.flowMVI.MVIIntent
import com.nek12.flowMVI.MVIState
import com.nek12.flowMVI.MVIStore
import com.nek12.flowMVI.MVIStoreScope
import com.nek12.flowMVI.MVIStoreScopeImpl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
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
import kotlinx.coroutines.yield
import java.util.concurrent.atomic.AtomicBoolean

internal abstract class BaseStore<S: MVIState, in I: MVIIntent, A: MVIAction>(
    initialState: S,
    @BuilderInference private val recover: MVIStoreScope<S, I, A>.(e: Exception) -> S,
    @BuilderInference private val reduce: suspend MVIStoreScope<S, I, A>.(I) -> S,
): MVIStore<S, I, A> {

    private val _states = MutableStateFlow(initialState)
    override val states: StateFlow<S> = _states.asStateFlow()
    private val isLaunched = AtomicBoolean(false)

    private val intents = Channel<I>(Channel.UNLIMITED, SUSPEND)

    override fun set(state: S) {
        _states.update { state }
    }

    override fun launch(scope: CoroutineScope): Job {
        require(!isLaunched.getAndSet(true)) { "Store is already launched" }

        return scope.launch {
            val childScope = MVIStoreScopeImpl(this + SupervisorJob(), this@BaseStore)
            while (isActive) {
                set(
                    try {
                        childScope.reduce(intents.receive())
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        childScope.recover(e)
                    }
                )
                yield()
            }
        }.apply {
            invokeOnCompletion { isLaunched.set(false) }
        }
    }

    override fun send(intent: I) {
        intents.trySend(intent)
    }
}
