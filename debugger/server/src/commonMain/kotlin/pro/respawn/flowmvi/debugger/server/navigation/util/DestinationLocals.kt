package pro.respawn.flowmvi.debugger.server.navigation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import org.koin.compose.LocalKoinScope
import pro.respawn.flowmvi.compose.dsl.LocalSubscriberLifecycle
import pro.respawn.flowmvi.compose.dsl.rememberSubscriberLifecycle
import pro.respawn.flowmvi.debugger.server.di.LocalDestinationScope
import pro.respawn.flowmvi.debugger.server.navigation.component.DestinationComponent
import pro.respawn.flowmvi.essenty.lifecycle.asSubscriberLifecycle

@Composable
internal fun ProvideDestinationLocals(
    component: DestinationComponent,
    content: @Composable () -> Unit
) = CompositionLocalProvider(
    LocalSubscriberLifecycle provides rememberSubscriberLifecycle(component.lifecycle) { asSubscriberLifecycle },
    LocalDestinationScope provides component,
    LocalKoinScope provides component.scope,
    content = content,
)
