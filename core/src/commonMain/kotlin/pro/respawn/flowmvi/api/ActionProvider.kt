package pro.respawn.flowmvi.api

import kotlinx.coroutines.flow.Flow

/**
 * An entity that can provide [MVIAction]s through the [actions] flow.
 * This flow may behave differently depending on [ActionShareBehavior] chosen.
 * This is mainly implemented by the [Store] and exposed through [Provider] ([Store.subscribe])
 */
public interface ActionProvider<out A : MVIAction> {

    /**
     * A flow of [MVIAction]s to be handled by the subscribers,
     * usually resulting in one-shot events.
     * How actions are distributed depends on [ActionShareBehavior].
     */
    public val actions: Flow<A>
}
