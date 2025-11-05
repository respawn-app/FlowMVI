package pro.respawn.flowmvi.api.context

import kotlinx.coroutines.flow.StateFlow

/**
 * An entity that is aware of the number of its subscribers. Usually [pro.respawn.flowmvi.api.PipelineContext]
 */
public interface SubscriptionAware {

    /**
     * The current number of subscribers of this entity.
     *
     * This flow is an input
     * to [pro.respawn.flowmvi.util.doWhileSubscribed] or [pro.respawn.flowmvi.plugins.whileSubscribedPlugin].
     *
     * This flow is usually not collected directly, but rather used by the store's plugins.
     */
    public val subscriberCount: StateFlow<Int>
}
