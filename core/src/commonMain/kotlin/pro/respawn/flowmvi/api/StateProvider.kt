package pro.respawn.flowmvi.api

import kotlinx.coroutines.flow.StateFlow

/**
 * An entity that exposes [states] and allows [StateConsumer]s to subscribe to them.
 * Most often accessed through a [Store] as a [Provider].
 */
public interface StateProvider<out S : MVIState> {

    /**
     * A flow of  states to be rendered by the subscriber.
     */
    public val states: StateFlow<S>
}
