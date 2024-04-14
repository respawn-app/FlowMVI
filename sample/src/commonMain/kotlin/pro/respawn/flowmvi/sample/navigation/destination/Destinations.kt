package pro.respawn.flowmvi.sample.navigation.destination

import androidx.compose.runtime.Composable
import pro.respawn.flowmvi.sample.features.home.HomeScreen
import pro.respawn.flowmvi.sample.navigation.component.RootComponent

@Composable
fun Destinations(
    destination: Destination,
    component: RootComponent
) = when (destination) {
    Destination.Home -> HomeScreen(component)
}
