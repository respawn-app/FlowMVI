package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StateReceiver
import pro.respawn.flowmvi.api.StateProvider
import pro.respawn.flowmvi.util.typed
import pro.respawn.flowmvi.util.withType
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Run [block] if current [StateProvider.states] value is of type [T], otherwise do nothing.
 *
 * **This function will suspend until all previous [StateReceiver.withState] invocations are finished.**
 * @see StateReceiver.withState
 * @see StateReceiver.useState
 * @see StateReceiver.updateState
 */
@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState> StateReceiver<S>.withState(
    @BuilderInference crossinline block: suspend T.() -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    withState { typed<T>()?.block() }
}

/**
 * Obtain the current [StateProvider.states] value and update it with
 * the result of [transform] if it is of type [T], otherwise do nothing.
 *
 * **This function will suspend until all previous [StateReceiver.withState] invocations are finished.**
 * @see StateReceiver.withState
 * @see StateReceiver.useState
 * @see StateReceiver.updateState
 */
@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState> StateReceiver<S>.updateState(
    @BuilderInference crossinline transform: suspend T.() -> S
) {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return updateState { withType<T, _> { transform() } }
}

/**
 * Obtain the current [StateProvider.states] value and update it with
 * the result of [transform] if it is of type [T], otherwise do nothing.
 *
 * * **This function may be executed multiple times**
 * * **This function will not trigger any plugins. It is intended for performance-critical operations only**
 * * **This function does lock the state. Watch out for races**
 *
 * @see StateReceiver.withState
 * @see StateReceiver.useState
 * @see StateReceiver.updateState
 */
@FlowMVIDSL
public inline fun <reified T : S, S : MVIState> StateReceiver<S>.useState(
    @BuilderInference crossinline transform: T.() -> S
): Unit = useState { withType<T, _> { transform() } }
