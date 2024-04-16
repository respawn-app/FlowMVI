package pro.respawn.flowmvi.sample.navigation

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import pro.respawn.flowmvi.sample.navigation.component.RootComponent
import pro.respawn.flowmvi.sample.navigation.destination.Destinations
import pro.respawn.flowmvi.sample.navigation.util.ProvideDestinationLocals
import pro.respawn.flowmvi.sample.navigation.util.defaultNavAnimation
import pro.respawn.flowmvi.sample.ui.theme.RespawnTheme

@Composable
fun AppContent(
    root: RootComponent
) = ProvideDestinationLocals(root) {
    RespawnTheme(useDynamicColors = true) {
        Children(root.stack, animation = defaultNavAnimation(root)) { child ->
            ProvideDestinationLocals(child.instance) {
                Destinations(
                    component = child.instance,
                    destination = child.configuration,
                    navigator = root,
                )
            }
        }
    }
}
