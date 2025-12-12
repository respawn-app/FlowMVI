package pro.respawn.flowmvi.debugger.server.navigation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import org.koin.compose.ComposeContextWrapper
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import pro.respawn.flowmvi.compose.dsl.LocalSubscriberLifecycle
import pro.respawn.flowmvi.compose.dsl.rememberSubscriberLifecycle
import pro.respawn.flowmvi.debugger.server.di.LocalDestinationScope
import pro.respawn.flowmvi.debugger.server.navigation.component.DestinationComponent
import pro.respawn.flowmvi.essenty.lifecycle.asSubscriberLifecycle

@OptIn(KoinInternalApi::class)
@Composable
internal fun ProvideDestinationLocals(
    component: DestinationComponent,
    content: @Composable () -> Unit
) = CompositionLocalProvider(
    LocalSubscriberLifecycle provides rememberSubscriberLifecycle(component.lifecycle) { asSubscriberLifecycle },
    LocalDestinationScope provides component,
    LocalKoinScope provides ComposeContextWrapper(component.scope),
    content = content,
)
