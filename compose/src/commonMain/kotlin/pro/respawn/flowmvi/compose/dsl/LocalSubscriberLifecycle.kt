package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLifecycleOwner
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.compose.api.SubscriberLifecycle

/**
 * A local composition [SubscriberLifecycle] instance. May return `null` if no lifecycle was provided.
 * Can be provided with [ProvideSubscriberLifecycle].
 */
@FlowMVIDSL
public val LocalSubscriberLifecycle: ProvidableCompositionLocal<SubscriberLifecycle?> = staticCompositionLocalOf {
    null
}

/** Provides [LocalSubscriberLifecycle] with an instance of [lifecycle] for child [content].
 *
 */
@FlowMVIDSL
@Composable
public fun ProvideSubscriberLifecycle(
    lifecycle: SubscriberLifecycle,
    content: @Composable () -> Unit
): Unit = CompositionLocalProvider(
    LocalSubscriberLifecycle provides lifecycle,
    content = content,
)

/**
 * Remember a new subscriber lifecycle instance from [delegate] to convert it using [factory]
 */
@FlowMVIDSL
@Composable
public fun <T> rememberSubscriberLifecycle(
    delegate: T,
    factory: T.() -> SubscriberLifecycle
): SubscriberLifecycle = remember(delegate) { delegate.factory() }

/**
 * Get the current provided subscriber lifecycle, or if not found, fall back to the platform-provided lifecycle
 */
@FlowMVIDSL
public val CurrentLifecycle: SubscriberLifecycle
    @Composable
    get() = LocalSubscriberLifecycle.current
        ?: rememberSubscriberLifecycle(LocalLifecycleOwner.current) { lifecycle.asSubscriberLifecycle }
