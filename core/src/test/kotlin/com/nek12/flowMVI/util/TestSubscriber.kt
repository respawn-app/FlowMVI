package com.nek12.flowMVI.util

import com.nek12.flowMVI.MVIAction
import com.nek12.flowMVI.MVIProvider
import com.nek12.flowMVI.MVIState
import com.nek12.flowMVI.MVISubscriber
import com.nek12.flowMVI.subscribe
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

class TestSubscriber<S : MVIState, A : MVIAction> : MVISubscriber<S, A> {

    private val _states by atomic(mutableListOf<S>())
    val states: List<S> get() = _states

    private val _actions by atomic(mutableListOf<A>())
    val actions: List<A> get() = _actions

    override fun render(state: S) {
        _states.add(state)
    }

    override fun consume(action: A) {
        _actions.add(action)
    }

    fun reset() {
        _states.clear()
        _actions.clear()
    }

    suspend inline fun subscribed(
        provider: MVIProvider<S, *, A>,
        scope: CoroutineScope,
        test: TestSubscriber<S, A>.() -> Unit
    ) = subscribe(provider, scope).apply {
        test()
        cancel()
        join()
        reset()
    }
}
