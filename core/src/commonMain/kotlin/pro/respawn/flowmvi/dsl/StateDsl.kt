package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StateReceiver
import pro.respawn.flowmvi.exceptions.InvalidStateException
import pro.respawn.flowmvi.util.typed
import pro.respawn.flowmvi.util.withType

/**
 * A typed overload of [StateReceiver.withState].
 */
@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState> StateReceiver<S>.withState(
    @BuilderInference crossinline block: suspend T.() -> Unit
) = withState { typed<T>()?.block() }

/**
 * A typed overload of [StateReceiver.updateState].
 */
@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState> StateReceiver<S>.updateState(
    @BuilderInference crossinline transform: suspend T.() -> S
) = updateState { withType<T, _> { transform() } }

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
) = updateStateImmediate { withType<T, _> { transform() } }

/**
 * Use the state if it is of type [T], otherwise, throw [InvalidStateException].
 *
 * Same rules apply as [StateReceiver.withState]
 */
@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState> PipelineContext<S, *, *>.withStateOrThrow(
    @BuilderInference crossinline block: suspend T.() -> Unit
) = withState {
    typed<T>()?.block() ?: throw InvalidStateException(T::class.simpleName, this::class.simpleName)
}

/**
 * Update the state if it is of type [T], otherwise throw [InvalidStateException].
 *
 * Same rules apply as [StateReceiver.updateState]
 */
@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState> PipelineContext<S, *, *>.updateStateOrThrow(
    @BuilderInference crossinline transform: suspend T.() -> S
) = updateState {
    typed<T>()?.transform() ?: throw InvalidStateException(T::class.simpleName, this::class.simpleName)
}

// region deprecated

@FlowMVIDSL
@Suppress("UndocumentedPublicFunction")
@Deprecated("renamed to updateStateImmediate()", ReplaceWith("updateStateImmediate(block)"))
public inline fun <reified T : S, S : MVIState> StateReceiver<S>.useState(
    @BuilderInference crossinline transform: T.() -> S
): Unit = updateStateImmediate { withType<T, _> { transform() } }

// endregion
