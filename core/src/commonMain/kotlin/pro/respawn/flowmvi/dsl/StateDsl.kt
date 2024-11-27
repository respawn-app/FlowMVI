package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StateReceiver
import pro.respawn.flowmvi.api.UnrecoverableException
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

@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState> PipelineContext<S, *, *>.withStateOrThrow(
    @BuilderInference crossinline block: suspend T.() -> Unit
) = withState {
    typed<T>()?.block() ?: run {
        if (config.debuggable) throw UnrecoverableException(message = "State is not of type ${T::class.simpleName}")
    }
}

@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState> PipelineContext<S, *, *>.updateStateOrThrow(
    @BuilderInference crossinline transform: suspend T.() -> S
) = updateState {
    typed<T>()?.transform() ?: run {
        if (config.debuggable) throw UnrecoverableException(message = "State is not of type ${T::class.simpleName}")
        this
    }
}

// region deprecated

@FlowMVIDSL
@Suppress("UndocumentedPublicFunction")
@Deprecated("renamed to updateStateImmediate()", ReplaceWith("updateStateImmediate(block)"))
public inline fun <reified T : S, S : MVIState> StateReceiver<S>.useState(
    @BuilderInference crossinline transform: T.() -> S
): Unit = updateStateImmediate { withType<T, _> { transform() } }

// endregion
