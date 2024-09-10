package pro.respawn.flowmvi.debugger.server.navigation.destination

import androidx.compose.runtime.Composable
import pro.respawn.flowmvi.debugger.server.navigation.AppNavigator
import pro.respawn.flowmvi.debugger.server.navigation.component.DestinationComponent
import pro.respawn.flowmvi.debugger.server.navigation.util.ProvideDestinationLocals
import pro.respawn.flowmvi.debugger.server.ui.screens.connect.ConnectScreen
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineScreen

@Composable
fun Destinations(
    destination: Destination,
    navigator: AppNavigator,
    component: DestinationComponent,
) = ProvideDestinationLocals(component) {
    when (destination) {
        Destination.Timeline -> TimelineScreen(navigator)
        Destination.Connect -> ConnectScreen(navigator)
    }
}
