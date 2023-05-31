package pro.respawn.flowmvi.base

import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.dsl.DelicateStoreApi
import pro.respawn.flowmvi.dsl.FlowMVIDSL

public interface StateReceiver<S : MVIState> {

    /**
     * Obtain the current [MVIStore.state] and update it with the result of [transform].
     *
     * **This function will suspend until all previous [MVIStore.withState] invocations are finished.**
     * **This function is reentrant, for more info, see [MVIStore.withState].**
     *
     * If you want to operate on a state of particular subtype, use the typed version of this function.
     * @see [withState]
     */
    @FlowMVIDSL
    public suspend fun updateState(transform: suspend S.() -> S)

    /**
     * Obtain the current state and operate on it, returning [R].
     *
     * This function does NOT update the state, for that, use [updateState].
     * Store allows only one state update at a time, and because of that,
     *
     * **every coroutine that will invoke [withState] or [updateState]
     * will be suspended until the previous update is finished.**
     *
     * This function uses locks under the hood.
     * For a version that runs when the state is of particular subtype, see other overloads of this function.
     *
     * This function is reentrant, which means, if you call:
     * ```kotlin
     * withState {
     *   withState { }
     * }
     * ```
     * you should not get a deadlock, but messing with coroutine contexts can still cause problems.
     *
     * @returns the value of [R], i.e. the result of the block.
     */
    @FlowMVIDSL
    public suspend fun <R> withState(block: suspend S.() -> R): R

    @FlowMVIDSL
    @DelicateStoreApi
    public fun useState(block: S.() -> S)
}
