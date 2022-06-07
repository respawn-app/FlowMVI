package util

import com.nek12.flowMVI.MVIAction
import com.nek12.flowMVI.MVIProvider
import com.nek12.flowMVI.MVIState
import com.nek12.flowMVI.MVISubscriber
import com.nek12.flowMVI.subscribe
import kotlinx.coroutines.CoroutineScope

class TestSubscriber<S: MVIState, A: MVIAction>(
    val render: (S) -> Unit = {},
    val consume: (A) -> Unit = {},
): MVISubscriber<S, A> {

    var counter = 0
    private set

    private val _states = mutableListOf<S>()
    val states: List<S> get() = _states

    private val _actions = mutableListOf<A>()
    val actions: List<A> get() = _actions


    override fun render(state: S) {
        ++counter
        _states.add(state)
        render.invoke(state)
    }

    override fun consume(action: A) {
        ++counter
        _actions.add(action)
        consume.invoke(action)
    }

    fun reset() {
        _states.clear()
        _actions.clear()
    }

    suspend inline fun subscribed(provider: MVIProvider<S, *, A>, scope: CoroutineScope, test: TestSubscriber<S, A>.() -> Unit) =
        subscribe(provider, scope).apply {
            test()
            cancel()
            join()
            reset()
        }
}
