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
    component: RootComponent
) = ProvideDestinationLocals(component) {
    RespawnTheme(useDynamicColors = true) {
        Children(component.stack, animation = defaultNavAnimation(component)) { child ->
            ProvideDestinationLocals(child.instance) {
                Destinations(
                    destination = child.configuration,
                    component = component,
                )
            }
        }
    }
}
