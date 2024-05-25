package pro.respawn.flowmvi.api

/**
 * An entity that handles [MVIState] updates. This entity modifies the state of the [StateProvider].
 * This is most often implemented by a [Store] and exposed through [PipelineContext].
 */
public interface StateReceiver<S : MVIState> {

    /**
     * Obtain the current [StateProvider.state] and update it with the result of [transform].
     *
     * * **This function will suspend until all previous [withState] or [updateState] invocations are finished if
     * [StoreConfiguration.atomicStateUpdates] is enabled**
     * * **This function is reentrant, for more info, see [withState]**
     * * If you want to operate on a state of particular subtype, use the typed version of this function.
     * * If you wish to ignore plugins and thread-safety of state updates in favor of greater performance,
     * see [updateStateImmediate]
     *
     * @see [withState]
     * @see [updateStateImmediate]
     */
    @FlowMVIDSL
    public suspend fun updateState(transform: suspend S.() -> S)

    /**
     * Obtain the current state and operate on it without changing the state.
     *
     * * This function does NOT update the state, for that, use [updateState].
     * * **This function will suspend until all previous [withState] or [updateState] invocations are finished if
     * [StoreConfiguration.atomicStateUpdates] is enabled**
     * * If you want to operate on a state of particular subtype, use the typed version of this function.
     * * If you wish to ignore plugins and thread-safety of state updates in favor of greater performance,
     * see [updateStateImmediate]
     *
     * This function is reentrant, which means, if you call:
     * ```kotlin
     * withState {
     *   withState { }
     * }
     * ```
     * you will not get a deadlock.
     *
     * @returns the value of [S], i.e. the result of the block.
     */
    @FlowMVIDSL
    public suspend fun withState(block: suspend S.() -> Unit)

    /**
     * A function that obtains current state and updates it atomically (in the thread context), and non-atomically in
     * the coroutine context, which means it can cause races when you want to update states in parallel.
     *
     * This function is performant, but **ignores ALL plugins** and
     * **does not perform a serializable state transaction**
     *
     * It should only be used for the state updates that demand the highest performance and happen very often.
     * If [StoreConfiguration.atomicStateUpdates] is `false`, then this function is the same as [updateState]
     *
     * @see updateState
     * @see withState
     */
    @FlowMVIDSL
    public fun updateStateImmediate(block: S.() -> S)

    /**
     * Obtain the current value of state in an unsafe manner.
     * It is recommended to always use [withState] or [updateState] as obtaining this value can lead
     * to data races when the state transaction changes the value of the state previously obtained.
     */
    @DelicateStoreApi
    public val state: S

    // region deprecated

    @FlowMVIDSL
    @Suppress("UndocumentedPublicFunction")
    @Deprecated("renamed to updateStateImmediate()", ReplaceWith("updateStateImmediate(block)"))
    public fun useState(block: S.() -> S): Unit = updateStateImmediate(block)

    // endregion
}
