package pro.respawn.flowmvi.api

import kotlinx.coroutines.flow.Flow

public interface ActionProvider<out A : MVIAction> {

    /**
     * A flow of [MVIAction]s to be handled by the [MVISubscriber],
     * usually resulting in one-shot events.
     * How actions are distributed depends on [ActionShareBehavior].
     */
    public val actions: Flow<A>
}
