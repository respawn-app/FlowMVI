package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import pro.respawn.flowmvi.compose.api.SubscriberLifecycle

@get:Composable
internal expect val PlatformLifecycle: SubscriberLifecycle?

public val LocalSubscriberLifecycle: ProvidableCompositionLocal<SubscriberLifecycle?> = staticCompositionLocalOf {
    null
}

public val DefaultLifecycle: SubscriberLifecycle
    @Composable get() = LocalSubscriberLifecycle.current ?: PlatformLifecycle ?: ImmediateLifecycle
