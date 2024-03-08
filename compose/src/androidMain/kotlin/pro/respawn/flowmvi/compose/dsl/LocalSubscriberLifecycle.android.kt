package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLifecycleOwner
import pro.respawn.flowmvi.compose.android.asSubscriberOwner
import pro.respawn.flowmvi.compose.api.SubscriberLifecycleOwner

internal actual val PlatformLifecycle: SubscriberLifecycleOwner?
    @Composable get() = LocalLifecycleOwner.current.asSubscriberOwner()
