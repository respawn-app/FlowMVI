package pro.respawn.flowmvi.essenty.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import pro.respawn.flowmvi.compose.dsl.LocalSubscriberLifecycle
import pro.respawn.flowmvi.compose.dsl.requireLifecycle

/**
 * Provides a local Essenty lifecycle [owner] through a [LocalSubscriberLifecycle].
 * Can be used in conjunction with [requireLifecycle] afterwards
 */
@Composable
public fun ProvideSubscriberLifecycle(
    owner: LifecycleOwner,
    content: @Composable () -> Unit
): Unit = CompositionLocalProvider(
    LocalSubscriberLifecycle provides owner.asSubscriberLifecycle,
    content = content,
)