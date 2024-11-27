package pro.respawn.flowmvi.api

/**
 * An entity that handles [MVIState] updates. This entity modifies the state of the [StateProvider].
 * This is most often implemented by a [Store] and exposed through [PipelineContext].
 *
 * Implements [ImmediateStateReceiver]
 */
public interface StateReceiver<S : MVIState> : ImmediateStateReceiver<S> {

    /**
     * Obtain the current [StateProvider.state] and update it with the result of [transform].
     *
     * * **This function will suspend until all previous [withState] or [updateState] invocations are finished if
     * [StoreConfiguration.atomicStateUpdates] are enabled.**
     * * **This function is reentrant, for more info, see [withState]**
     * * If you want to operate on a state of particular subtype, use the typed version of this function.
     * * If you wish to ignore plugins and thread-safety of state updates in favor of greater performance,
     * see [ImmediateStateReceiver.updateStateImmediate].
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
     * [StoreConfiguration.atomicStateUpdates] are enabled.**
     * * If you want to operate on a state of particular subtype, use the typed version of this function.
     * * If you wish to ignore plugins and thread-safety of state updates in favor of greater performance,
     * see [ImmediateStateReceiver.updateStateImmediate]
     *
     * This function is reentrant, which means, if you call:
     * ```kotlin
     * withState {
     *   withState { }
     * }
     * ```
     * you will not get a deadlock even if the transaction is serializable.
     */
    @FlowMVIDSL
    public suspend fun withState(block: suspend S.() -> Unit)

    // region deprecated

    @FlowMVIDSL
    @Suppress("UndocumentedPublicFunction")
    @Deprecated("renamed to updateStateImmediate()", ReplaceWith("updateStateImmediate(block)"))
    public fun useState(block: S.() -> S): Unit = updateStateImmediate(block)

    // endregion
}
