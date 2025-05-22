package pro.respawn.flowmvi.util

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.map
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.context.SubscriptionAware
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 *  Runs [action] while [this] [SubscriptionAware] has at least [minSubscribers] subscribers.
 *  This function will suspend forever.
 *
 *  - Awaits until the [SubscriptionAware.subscriberCount] is at least [minSubscribers] and then invokes [action].
 *  - The [action] is canceled when the subscriber count drops below [minSubscribers] for longer than [stopDelay].
 *  - The [action] is restarted when the subscriber count reaches [minSubscribers] again.
 *  - The function will suspend until the [SubscriptionAware] is no longer needed (e.g. its scope is cancelled).
 *
 *  @param stopDelay the delay after which the [action] is cancelled if the subscriber count drops below [minSubscribers].
 *  @param minSubscribers the minimum number of subscribers to keep the [action] running.
 *  @param action the action to invoke when the subscriber count is sufficient.
 *
 *  @see pro.respawn.flowmvi.plugins.whileSubscribedPlugin
 */
@OptIn(FlowPreview::class)
@ExperimentalFlowMVIAPI
public suspend inline fun SubscriptionAware.whileSubscribed(
    stopDelay: Duration = 1.seconds,
    minSubscribers: Int = 1,
    crossinline action: suspend () -> Unit
): Unit = subscriberCount
    .map { it >= minSubscribers }
    .dropWhile { !it }
    .debounce { if (it) Duration.ZERO else stopDelay }
    .distinctUntilChanged()
    .collectLatest { if (it) action() }
