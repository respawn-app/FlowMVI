package pro.respawn.flowmvi.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.context.SubscriptionAware

/**
 * A mock implementation of [SubscriptionAware] for testing.
 * It's backed by an external [MutableStateFlow] to control the subscriber count.
 */
@OptIn(ExperimentalFlowMVIAPI::class)
class MockSubscriptionAware(
    private val _subscriberCount: MutableStateFlow<Int> = MutableStateFlow(0)
) : SubscriptionAware {

    @ExperimentalFlowMVIAPI
    override val subscriberCount: StateFlow<Int> = _subscriberCount

    /**
     * Set the subscriber count to the specified value.
     */
    fun setSubscriberCount(count: Int) {
        _subscriberCount.value = count
    }
}
