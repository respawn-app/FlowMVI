package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import pro.respawn.flowmvi.compose.api.SubscriberLifecycle

internal actual val PlatformLifecycle: SubscriberLifecycle? @Composable @ReadOnlyComposable get() = null
