package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.compose.api.SubscriberLifecycle

private const val MissingLifecycleError = """
Neither LocalSubscriberLifecycle nor PlatformLifecycle were provided for this function.
Please use ProvideSubscriberLifecycle() if you want to use composition locals for subscription.
"""

@get:Composable
internal expect val PlatformLifecycle: SubscriberLifecycle?

/**
 * A local composition [SubscriberLifecycle] instance. May return `null` if no lifecycle was provided.
 * Can be provided with [ProvideSubscriberLifecycle].
 */
public val LocalSubscriberLifecycle: ProvidableCompositionLocal<SubscriberLifecycle?> = staticCompositionLocalOf {
    null
}

/**
 * Tries to obtain a [LocalSubscriberLifecycle], then a platform lifecycle implementation, and if not found, throws
 * an [IllegalArgumentException]
 */
@Composable
public fun requireLifecycle(): SubscriberLifecycle = checkNotNull(
    LocalSubscriberLifecycle.current ?: PlatformLifecycle
) { MissingLifecycleError }

/**
 * Tries to obtain a [LocalSubscriberLifecycle], if not found, tries to fetch a platform lifecycle. If still not found,
 * uses a **default lifecycle that does not follow the system lifecycle** - [ImmediateLifecycle].
 * Use [requireLifecycle] if you want to prevent this function from falling back to a no-op lifecycle implementation.
 */
@FlowMVIDSL
public val DefaultLifecycle: SubscriberLifecycle
    @Composable get() = LocalSubscriberLifecycle.current
        ?: PlatformLifecycle
        ?: ImmediateLifecycle

/**
 * Provides a [LocalSubscriberLifecycle] in the scope of [content]
 */
@FlowMVIDSL
@Composable
public fun ProvideSubscriberLifecycle(
    lifecycleOwner: SubscriberLifecycle,
    content: @Composable () -> Unit
): Unit = CompositionLocalProvider(
    LocalSubscriberLifecycle provides lifecycleOwner,
    content = content,
)

/**
 * Remember a new subscriber lifecycle
 */
@FlowMVIDSL
@Composable
public fun <T> rememberSubscriberLifecycle(
    delegate: T,
    factory: T.() -> SubscriberLifecycle
): SubscriberLifecycle = remember(delegate) { delegate.factory() }
