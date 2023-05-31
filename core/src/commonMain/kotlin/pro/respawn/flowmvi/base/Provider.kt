package pro.respawn.flowmvi.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import pro.respawn.flowmvi.dsl.DelicateStoreApi
import pro.respawn.flowmvi.MVIState

public interface StateProvider<out S : MVIState> {

    /**
     * A flow of  states to be handled by the [MVISubscriber].
     */
    public val states: StateFlow<S>

    /**
     * Obtain the current state in an unsafe manner.
     * This property is not thread-safe and parallel state updates will introduce a race condition when not
     * handled properly.
     * Such race conditions arise when using multiple data streams such as [Flow]s
     */
    @DelicateStoreApi
    public val state: S get() = states.value
}
