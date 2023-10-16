package pro.respawn.flowmvi.api

/**
 * An entity that handles [MVIState] updates. This entity modifies the state of the [StateProvider].
 * This is most often implemented by a [Store] and exposed through [PipelineContext].
 */
public interface StateReceiver<S : MVIState> {

    /**
     * Obtain the current [StateProvider.state] and update it with the result of [transform]
     * atomically and in a suspending manner.
     *
     * **This function will suspend until all previous [withState] or [updateState] invocations are finished.**
     * **This function is reentrant, for more info, see [withState].**
     *
     * If you want to operate on a state of particular subtype, use the typed version of this function.
     *
     * @see [withState]
     */
    @FlowMVIDSL
    public suspend fun updateState(transform: suspend S.() -> S)

    /**
     * Obtain the current state and operate on it, returning [R].
     *
     * This function does NOT update the state, for that, use [updateState].
     * Store allows only one state update at a time, and because of that,
     * **every coroutine that will invoke [withState] or [updateState]
     * will be suspended until the previous state handler is finished.**
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
     * you should not get a deadlock, but overriding coroutine contexts can still cause problems.
     * This function has lower performance than [useState] and allows plugins to intercept the state change.
     * If you really need the additional performance or wish to avoid plugins, use [useState].
     *
     * @returns the value of [R], i.e. the result of the block.
     */
    @FlowMVIDSL
    public suspend fun withState(block: suspend S.() -> Unit)

    /**
     * A function that obtains current state and updates it atomically (in the thread context), and non-atomically in
     * the coroutine context, which means it can cause races when you want to update states in parallel.
     * This function is performant, but **circumvents ALL plugins** and is **not thread-safe**.
     * It should only be used for the most critical state updates happening very often.
     */
    @FlowMVIDSL
    public fun useState(block: S.() -> S)
}
