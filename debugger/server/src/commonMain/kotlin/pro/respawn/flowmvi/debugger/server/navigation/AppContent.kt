package pro.respawn.flowmvi.debugger.server.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.koin.compose.KoinContext
import pro.respawn.flowmvi.debugger.server.di.koin
import pro.respawn.flowmvi.debugger.server.navigation.component.RootComponent
import pro.respawn.flowmvi.debugger.server.navigation.destination.Destinations
import pro.respawn.flowmvi.debugger.server.navigation.util.defaultNavAnimation
import pro.respawn.flowmvi.debugger.server.ui.theme.RespawnTheme
import pro.respawn.flowmvi.debugger.server.ui.widgets.DynamicTwoPaneLayout
import pro.respawn.kmmutils.compose.windowsize.isWideScreen

@Composable
fun AppContent(
    root: RootComponent
) = KoinContext(koin) {
    RespawnTheme {
        val navigator = rememberAppNavigator(isWideScreen, root)
        val details by root.details.details.subscribeAsState()
        DynamicTwoPaneLayout(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            secondPaneVisible = details.child != null && isWideScreen,
            firstPaneContent = {
                Children(
                    stack = root.stack,
                    animation = defaultNavAnimation(root),
                ) { child ->
                    Destinations(
                        component = child.instance,
                        destination = child.configuration,
                        navigator = navigator,
                    )
                }
            },
            secondaryPaneContent = pane@{
                Crossfade(details.child) { value ->
                    Destinations(
                        component = value?.instance ?: return@Crossfade,
                        destination = value.configuration,
                        navigator = navigator,
                    )
                }
            },
        )
    }

}
