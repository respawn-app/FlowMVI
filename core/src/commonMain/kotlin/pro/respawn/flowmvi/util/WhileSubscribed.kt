package pro.respawn.flowmvi.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.context.SubscriptionAware
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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

@ExperimentalFlowMVIAPI
public inline fun <T> T.whileSubscribed(
    stopDelay: Duration = 1.seconds,
    minSubscribers: Int = 1,
    crossinline action: suspend () -> Unit
): Job where T : SubscriptionAware, T : CoroutineScope = launch(start = CoroutineStart.UNDISPATCHED) {
    (this as SubscriptionAware).whileSubscribed(stopDelay, minSubscribers, action)
}
