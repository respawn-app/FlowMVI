package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StateReceiver
import pro.respawn.flowmvi.util.typed
import pro.respawn.flowmvi.util.withType
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A typed overload of [StateReceiver.withState].
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
 * A typed overload of [StateReceiver.updateState].
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
 * A typed overload of [StateReceiver.updateStateImmediate].
 *
 * @see StateReceiver.withState
 * @see StateReceiver.updateStateImmediate
 * @see StateReceiver.updateState
 */
@FlowMVIDSL
public inline fun <reified T : S, S : MVIState> StateReceiver<S>.updateStateImmediate(
    @BuilderInference crossinline transform: T.() -> S
) {
    contract {
        callsInPlace(transform)
    }
    updateStateImmediate { withType<T, _> { transform() } }
}

// region deprecated

@FlowMVIDSL
@Suppress("UndocumentedPublicFunction")
@Deprecated("renamed to updateStateImmediate()", ReplaceWith("updateStateImmediate(block)"))
public inline fun <reified T : S, S : MVIState> StateReceiver<S>.useState(
    @BuilderInference crossinline transform: T.() -> S
): Unit = updateStateImmediate { withType<T, _> { transform() } }

// endregion
