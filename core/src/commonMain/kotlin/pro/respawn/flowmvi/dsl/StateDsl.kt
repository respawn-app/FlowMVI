package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.MVIStore
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StateReceiver
import pro.respawn.flowmvi.updateState
import pro.respawn.flowmvi.util.typed
import pro.respawn.flowmvi.util.withType
import pro.respawn.flowmvi.withState
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Run [block] if current [MVIStore.state] is of type [T], otherwise do nothing.
 *
 * **This function will suspend until all previous [MVIStore.withState] invocations are finished.**
 * @see MVIStore.withState
 * @see MVIStore.useState
 * @see MVIStore.updateState
 */
@OverloadResolutionByLambdaReturnType
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
 * Obtain the current [MVIStore.state] and update it with
 * the result of [transform] if it is of type [T], otherwise do nothing.
 *
 * **This function will suspend until all previous [MVIStore.withState] invocations are finished.**
 * @see MVIStore.withState
 * @see MVIStore.useState
 * @see MVIStore.updateState
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
 * Obtain the current [MVIStore.state] and update it with
 * the result of [transform] if it is of type [T], otherwise do nothing.
 *
 * * **This function may be executed multiple times**
 * * **This function will not trigger any plugins. It is intended for performance-critical operations only**
 * * **This function does lock the state. Watch out for races**
 *
 * @see MVIStore.withState
 * @see MVIStore.useState
 * @see MVIStore.updateState
 */
@FlowMVIDSL
public inline fun <reified T : S, S : MVIState> StateReceiver<S>.useState(
    @BuilderInference crossinline transform: T.() -> S
): S = useState { withType<T, _> { transform() } }
