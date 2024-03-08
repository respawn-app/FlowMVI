package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Composable
import pro.respawn.flowmvi.compose.api.SubscriberLifecycle

internal actual val PlatformLifecycle: SubscriberLifecycle? @Composable get() = null
