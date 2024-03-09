package pro.respawn.flowmvi.compose.dsl

import kotlinx.coroutines.coroutineScope
import pro.respawn.flowmvi.compose.api.SubscriberLifecycle
import pro.respawn.flowmvi.compose.api.SubscriptionMode

/**
 * A no-op [SubscriberLifecycle] implementation that does not follow the system lifecycle in any way and ignores
 * [SubscriptionMode]
 */
public val ImmediateLifecycle: SubscriberLifecycle = SubscriberLifecycle { _, block -> coroutineScope(block) }
