package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import pro.respawn.flowmvi.compose.api.SubscriberLifecycle

public val LocalSubscriberLifecycle: ProvidableCompositionLocal<SubscriberLifecycle?> = staticCompositionLocalOf {
    null
}

@get:Composable
internal expect val PlatformLifecycle: SubscriberLifecycle?

@PublishedApi
internal val CurrentLifecycle: SubscriberLifecycle
    @Composable get() = LocalSubscriberLifecycle.current ?: PlatformLifecycle ?: ImmediateLifecycle
