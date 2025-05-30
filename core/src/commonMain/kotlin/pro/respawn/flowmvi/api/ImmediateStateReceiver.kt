package pro.respawn.flowmvi.api

import kotlinx.coroutines.flow.StateFlow
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.dsl.updateStateImmediate

/**
 * [StateReceiver] version that can only accept immediate state updates. It is recommended to use [StateReceiver] and
 * its methods if possible. See the method docs for details
 */
public interface ImmediateStateReceiver<S : MVIState> : StateProvider<S> {

    /**
     * Directly compare and set the current state.
     *
     * Please read [updateStateImmediate] to learn about repercussions of using this.
     */
    @InternalFlowMVIAPI
    public fun compareAndSet(old: S, new: S): Boolean

    @InternalFlowMVIAPI
    override val states: StateFlow<S>
}
