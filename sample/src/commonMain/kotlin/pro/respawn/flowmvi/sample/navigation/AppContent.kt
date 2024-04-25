package pro.respawn.flowmvi.sample.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import pro.respawn.flowmvi.sample.navigation.component.RootComponent
import pro.respawn.flowmvi.sample.navigation.destination.Destinations
import pro.respawn.flowmvi.sample.navigation.util.LocalWindowSize
import pro.respawn.flowmvi.sample.navigation.util.NavigationDefaults
import pro.respawn.flowmvi.sample.navigation.util.defaultNavAnimation
import pro.respawn.flowmvi.sample.ui.theme.RespawnTheme
import pro.respawn.flowmvi.sample.ui.widgets.DynamicTwoPaneLayout

@Composable
fun AppContent(
    root: RootComponent
) = RespawnTheme(useDynamicColors = true) {
    val isWindowWide = LocalWindowSize.width > NavigationDefaults.WideScreenWidth
    val navigator = rememberAppNavigator(isWindowWide, root)
    val details by root.details.details.subscribeAsState()
    DynamicTwoPaneLayout(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        secondPaneVisible = details.child != null && isWindowWide,
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
