package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Composable
import pro.respawn.flowmvi.compose.api.SubscriberLifecycleOwner

internal actual val PlatformLifecycle: SubscriberLifecycleOwner? @Composable get() = null
