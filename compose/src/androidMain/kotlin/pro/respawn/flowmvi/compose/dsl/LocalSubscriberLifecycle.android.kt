package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import pro.respawn.flowmvi.compose.android.asSubscriberLifecycle
import pro.respawn.flowmvi.compose.api.SubscriberLifecycle

internal actual val PlatformLifecycle: SubscriberLifecycle?
    @Composable get() = rememberSubscriberLifecycle(LocalLifecycleOwner.current.lifecycle) { asSubscriberLifecycle }
