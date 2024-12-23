package pro.respawn.flowmvi.api

import kotlinx.coroutines.CoroutineScope

/**
 * A subscriber lifecycle is the lifecycle of the [Consumer]. This is usually the UI lifecycle.
 *
 * The lifecycle implementation must follow the [SubscriptionMode] contract as described in the documentation.
 */
public interface SubscriberLifecycle {

    /**
     * Repeat the execution of the [block] using the specified [mode].
     *
     * This means that if the [mode] is no longer valid i.e. screen is not [SubscriptionMode.Visible], the
     * [block] parameter must be cancelled, and invoked again when the condition is satisfied again.
     *
     * @see SubscriptionMode
     */
    public suspend fun repeatOnLifecycle(mode: SubscriptionMode, block: suspend CoroutineScope.() -> Unit)

    public override fun equals(other: Any?): Boolean
    public override fun hashCode(): Int
}
