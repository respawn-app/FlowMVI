package pro.respawn.flowmvi.decompose.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import pro.respawn.flowmvi.compose.dsl.LocalSubscriberLifecycle

@Composable
public fun ProvideSubscriberLifecycle(
    owner: LifecycleOwner,
    content: @Composable () -> Unit
): Unit = CompositionLocalProvider(
    LocalSubscriberLifecycle provides owner.asSubscriberLifecycle,
    content = content,
)
