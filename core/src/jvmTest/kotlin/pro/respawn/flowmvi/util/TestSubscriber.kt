package pro.respawn.flowmvi.util

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIProvider
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.MVISubscriber
import pro.respawn.flowmvi.dsl.subscribe

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
