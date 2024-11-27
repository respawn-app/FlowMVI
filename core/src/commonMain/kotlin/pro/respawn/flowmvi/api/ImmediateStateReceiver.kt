package pro.respawn.flowmvi.api

/**
 * [StateReceiver] version that can only accept immediate state updates. It is recommended to use [StateReceiver] and
 * its methods if possible. See the method docs for details
 */
public interface ImmediateStateReceiver<S : MVIState> {

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
    @FlowMVIDSL
    public fun updateStateImmediate(block: S.() -> S)

    /**
     * Obtain the current value of state in an unsafe manner.
     * It is recommended to always use [StateReceiver.withState] or [StateReceiver.updateState] as obtaining this value can lead
     * to data races when the state transaction changes the value of the state previously obtained.
     */
    @DelicateStoreApi
    public val state: S
}
