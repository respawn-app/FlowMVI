package pro.respawn.flowmvi.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * An entity that exposes [states] and allows [StateConsumer]s to subscribe to them.
 * Most often accessed through a [Store] as a [Provider].
 */
public interface StateProvider<out S : MVIState> {

    /**
     * A flow of  states to be handled by the subscriber.
     */
    public val states: StateFlow<S>

    /**
     * Obtain the current state in an unsafe manner.
     * This property is not thread-safe and parallel state updates will introduce a race condition when not
     * handled properly.
     * Such race conditions arise when using multiple data streams such as [Flow]s.
     *
     * Accessing and modifying the state this way will **circumvent ALL plugins** and will not make state updates atomic.
     *
     * Consider accessing state via [StateReceiver.withState] or [StateReceiver.updateState] instead.
     */
    @DelicateStoreApi
    public val state: S
}
