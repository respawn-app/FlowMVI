package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.MVIStore
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StateReceiver
import pro.respawn.flowmvi.updateState
import pro.respawn.flowmvi.util.withType
import pro.respawn.flowmvi.withState
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

/**
 * Run [block] if current [MVIStore.state] is of type [T], otherwise do nothing.
 *
 * **This function will suspend until all previous [MVIStore.withState] invocations are finished.**
 * @see MVIStore.withState
 */
@OverloadResolutionByLambdaReturnType
@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState, R> StateReceiver<S>.withState(
    @BuilderInference crossinline block: suspend T.() -> R
): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return withState { (this as? T)?.let { it.block() } }
}

/**
 * Obtain the current [MVIStore.state] and update it with
 * the result of [transform] if it is of type [T], otherwise do nothing.
 *
 * **This function will suspend until all previous [MVIStore.withState] invocations are finished.**
 * @see MVIStore.updateState
 * @see [withState]
 */
@JvmName("updateStateTyped")
@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState> StateReceiver<S>.updateState(
    @BuilderInference crossinline transform: suspend T.() -> S
) {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return updateState { withType<T, _> { transform() } }
}
