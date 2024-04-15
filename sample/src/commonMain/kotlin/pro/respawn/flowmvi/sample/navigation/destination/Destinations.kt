package pro.respawn.flowmvi.sample.navigation.destination

import androidx.compose.runtime.Composable
import pro.respawn.flowmvi.sample.features.decompose.DecomposeScreen
import pro.respawn.flowmvi.sample.features.diconfig.DiConfigScreen
import pro.respawn.flowmvi.sample.features.home.HomeScreen
import pro.respawn.flowmvi.sample.features.lce.LCEScreen
import pro.respawn.flowmvi.sample.features.logging.LoggingScreen
import pro.respawn.flowmvi.sample.features.savedstate.SavedStateScreen
import pro.respawn.flowmvi.sample.features.simple.SimpleScreen
import pro.respawn.flowmvi.sample.features.undoredo.UndoRedoScreen
import pro.respawn.flowmvi.sample.navigation.component.DestinationComponent
import pro.respawn.flowmvi.sample.navigation.component.RootComponent

@Composable
fun Destinations(
    destination: Destination,
    navigator: RootComponent,
    component: DestinationComponent,
) = when (destination) {
    is Destination.Home -> HomeScreen(navigator)
    is Destination.SimpleFeature -> SimpleScreen(navigator)
    is Destination.LCEFeature -> LCEScreen(navigator)
    is Destination.SavedState -> SavedStateScreen(navigator)
    is Destination.DiConfig -> DiConfigScreen(navigator)
    is Destination.Logging -> LoggingScreen(navigator)
    is Destination.UndoRedo -> UndoRedoScreen(navigator)
    is Destination.Decompose -> DecomposeScreen(component, navigator)
}
