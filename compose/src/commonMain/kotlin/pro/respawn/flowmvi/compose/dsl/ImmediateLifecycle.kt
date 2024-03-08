package pro.respawn.flowmvi.compose.dsl

import kotlinx.coroutines.coroutineScope
import pro.respawn.flowmvi.compose.api.SubscriberLifecycle

@PublishedApi
internal val ImmediateLifecycle: SubscriberLifecycle = SubscriberLifecycle { _, block -> coroutineScope(block) }
