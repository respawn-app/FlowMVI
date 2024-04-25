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
import pro.respawn.flowmvi.sample.navigation.AppNavigator
import pro.respawn.flowmvi.sample.navigation.component.DestinationComponent
import pro.respawn.flowmvi.sample.navigation.util.ProvideDestinationLocals

@Composable
fun Destinations(
    destination: Destination,
    navigator: AppNavigator,
    component: DestinationComponent,
) = ProvideDestinationLocals(component) {
    when (destination) {
        Destination.Home -> HomeScreen(navigator)
        Destination.SimpleFeature -> SimpleScreen(navigator)
        Destination.LCEFeature -> LCEScreen(navigator)
        Destination.SavedState -> SavedStateScreen(navigator)
        Destination.DiConfig -> DiConfigScreen(navigator)
        Destination.Logging -> LoggingScreen(navigator)
        Destination.UndoRedo -> UndoRedoScreen(navigator)
        Destination.Decompose -> DecomposeScreen(component, navigator)
    }
}
