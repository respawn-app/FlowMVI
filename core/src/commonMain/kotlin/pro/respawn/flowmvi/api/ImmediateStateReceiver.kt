package pro.respawn.flowmvi.api

/**
 * [StateReceiver] version that can only accept immediate state updates. It is recommended to use [StateReceiver] and
 * its methods if possible. See the method docs for details
 */
public interface ImmediateStateReceiver<S : MVIState> {

    @FlowMVIDSL
    public fun compareAndSet(old: S, new: S): Boolean

    /**
     * Obtain the current value of state in an unsafe manner.
     * It is recommended to always use [StateReceiver.withState] or [StateReceiver.updateState] as obtaining this value can lead
     * to data races when the state transaction changes the value of the state previously obtained.
     */
    @DelicateStoreApi
    public val state: S
}
