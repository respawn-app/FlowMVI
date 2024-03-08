package pro.respawn.flowmvi.compose.dsl

import kotlinx.coroutines.coroutineScope
import pro.respawn.flowmvi.compose.api.SubscriberLifecycleOwner

@PublishedApi
internal val ImmediateLifecycleOwner: SubscriberLifecycleOwner = SubscriberLifecycleOwner { _, block ->
    coroutineScope(block)
}
