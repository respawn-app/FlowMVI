package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.ImmediateStateReceiver
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StateProvider
import pro.respawn.flowmvi.api.StateReceiver
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.exceptions.InvalidStateException
import pro.respawn.flowmvi.util.typed
import pro.respawn.flowmvi.util.withType
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

/**
 * Obtain the current state in an unsafe manner.
 *
 * This property is not thread-safe and parallel state updates will introduce a race condition when not
 * handled properly.
 * Such race conditions arise when using multiple data streams such as [Flow]s.
 *
 * Accessing and modifying the state this way will **circumvent ALL plugins** and will not make state updates atomic.
 *
 * Consider accessing state via [StateReceiver.withState] or [StateReceiver.updateState] instead.
 */
@DelicateStoreApi
public inline val <S : MVIState> StateProvider<S>.state: S get() = states.value

/**
 * A function that obtains current state and updates it atomically (in the thread context), and non-atomically in
 * the coroutine context, which means it can cause races when you want to update states in parallel.
 *
 * This function is performant, but **ignores ALL plugins** and
 * **does not perform a serializable state transaction**
 *
 * It should only be used for the state updates that demand the highest performance and happen very often.
 * If [StoreConfiguration.atomicStateUpdates] is `false`, then this function is the same
 * as [StateReceiver.updateState]
 *
 * @see StateReceiver.updateState
 * @see StateReceiver.withState
 */
@OptIn(DelicateStoreApi::class, InternalFlowMVIAPI::class)
@FlowMVIDSL
public inline fun <S : MVIState> ImmediateStateReceiver<S>.updateStateImmediate(
    @BuilderInference transform: S.() -> S
) {
    contract {
        callsInPlace(transform, InvocationKind.AT_LEAST_ONCE)
    }
    while (true) if (compareAndSet(state, transform(state))) return
}

/**
 * A typed overload of [StateReceiver.withState].
 */
@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState> StateReceiver<S>.withState(
    @BuilderInference crossinline block: suspend T.() -> Unit
): Unit = withState { typed<T>()?.block() }

/**
 * A typed overload of [StateReceiver.updateState].
 */
@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState> StateReceiver<S>.updateState(
    @BuilderInference crossinline transform: suspend T.() -> S
): Unit = updateState { withType<T, _> { transform() } }

/**
 * A typed overload of [updateStateImmediate].
 *
 * @see StateReceiver.withState
 * @see StateReceiver.updateState
 */
@FlowMVIDSL
@JvmName("updateStateImmediateTyped")
public inline fun <reified T : S, S : MVIState> ImmediateStateReceiver<S>.updateStateImmediate(
    @BuilderInference transform: T.() -> S
): Unit = updateStateImmediate { withType<T, _> { transform() } }

/**
 * Use the state if it is of type [T], otherwise, throw [InvalidStateException].
 *
 * Same rules apply as [StateReceiver.withState]
 */
@FlowMVIDSL
public suspend inline fun <reified T : S, S : MVIState> PipelineContext<S, *, *>.withStateOrThrow(
    @BuilderInference crossinline block: suspend T.() -> Unit
): Unit = withState {
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
): Unit = updateState {
    typed<T>()?.transform() ?: throw InvalidStateException(T::class.simpleName, this::class.simpleName)
}
