package pro.respawn.flowmvi.sample.navigation.destination

import androidx.compose.runtime.Composable
import pro.respawn.flowmvi.sample.features.home.HomeScreen
import pro.respawn.flowmvi.sample.features.lce.LCEScreen
import pro.respawn.flowmvi.sample.features.savedstate.SavedStateScreen
import pro.respawn.flowmvi.sample.features.simple.SimpleScreen
import pro.respawn.flowmvi.sample.navigation.AppNavigator
import pro.respawn.flowmvi.sample.navigation.component.DestinationComponent
import pro.respawn.flowmvi.sample.navigation.component.RootComponent
import pro.respawn.flowmvi.sample.navigation.util.ProvideDestinationLocals

@Composable
fun Destinations(
    destination: Destination,
    navigator: RootComponent,
) = when (destination) {
    is Destination.Home -> HomeScreen(navigator)
    is Destination.SimpleFeature -> SimpleScreen(navigator)
    is Destination.LCEFeature -> LCEScreen(navigator)
    is Destination.SavedState -> SavedStateScreen(navigator)
}
