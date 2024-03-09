package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalLifecycleOwner
import pro.respawn.flowmvi.compose.android.asSubscriberOwner
import pro.respawn.flowmvi.compose.api.SubscriberLifecycle

internal actual val PlatformLifecycle: SubscriberLifecycle?
    @Composable @ReadOnlyComposable get() = LocalLifecycleOwner.current.lifecycle.asSubscriberOwner()
