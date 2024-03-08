package pro.respawn.flowmvi.compose.api

import kotlinx.coroutines.CoroutineScope

public fun interface SubscriberLifecycle {

    public suspend fun repeatOnLifecycle(mode: SubscriptionMode, block: suspend CoroutineScope.() -> Unit)
}
