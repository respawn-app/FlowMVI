package pro.respawn.flowmvi.api.context

import kotlinx.coroutines.flow.StateFlow
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI

public interface SubscriptionAware {

    @ExperimentalFlowMVIAPI
    public val subscriberCount: StateFlow<Int>
}
