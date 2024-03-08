package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import pro.respawn.flowmvi.compose.api.SubscriberLifecycleOwner

public val LocalSubscriberLifecycle: ProvidableCompositionLocal<SubscriberLifecycleOwner?> = staticCompositionLocalOf {
    null
}

@PublishedApi
internal val CurrentLifecycle: SubscriberLifecycleOwner
    @Composable get() = LocalSubscriberLifecycle.current ?: PlatformLifecycle ?: ImmediateLifecycleOwner

@get:Composable
internal expect val PlatformLifecycle: SubscriberLifecycleOwner?
