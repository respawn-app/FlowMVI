package pro.respawn.flowmvi.compose.dsl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import pro.respawn.flowmvi.compose.api.SubscriberLifecycle

@Composable
public fun ProvideSubscriberLifecycle(
    lifecycleOwner: SubscriberLifecycle,
    content: @Composable () -> Unit
): Unit = CompositionLocalProvider(
    LocalSubscriberLifecycle provides lifecycleOwner,
    content = content,
)
