package pro.respawn.flowmvi.util

import kotlinx.atomicfu.atomic
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.storePlugin

class TestInfo<S : MVIState, I : MVIIntent, A : MVIAction> {

    val states by atomic(mutableListOf<S>())
    val actions by atomic(mutableListOf<A>())
    val intents by atomic(mutableListOf<I>())
    var subscriptions by atomic(0)
    var launches by atomic(0)
    var stops by atomic(0)

    fun reset() {
        states.clear()
        actions.clear()
        intents.clear()
        subscriptions = 0
        launches = 0
        stops = 0
    }
}

fun <S : MVIState, I : MVIIntent, A : MVIAction> testPlugin(
    subscriber: TestInfo<S, I, A>
) = storePlugin("TestPlugin") {
    onStart { subscriber.launches++ }
    onStop { subscriber.stops++ }
    onSubscribe { subscriber.subscriptions++ }
    onIntent { subscriber.intents.add(it); it }
    onAction { subscriber.actions.add(it); it }
    onState { _, new -> subscriber.states.add(new); new }
}
