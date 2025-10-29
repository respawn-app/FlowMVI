package pro.respawn.flowmvi.sample.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import org.koin.compose.koinInject
import pro.respawn.flowmvi.sample.navigation.component.RootComponent
import pro.respawn.flowmvi.sample.navigation.component.StackComponent
import pro.respawn.flowmvi.sample.navigation.destination.Destination
import pro.respawn.flowmvi.sample.navigation.details.DetailsComponent
import pro.respawn.flowmvi.sample.platform.PlatformFeatureLauncher

@Composable
fun rememberAppNavigator(
    isWideScreen: Boolean,
    root: RootComponent
): AppNavigator {
    val launcher = koinInject<PlatformFeatureLauncher>()
    return remember(isWideScreen, root) {
        AppNavigatorImpl(isWideScreen, root, root.details, launcher)
    }
}

private class AppNavigatorImpl(
    val isWideScreen: Boolean,
    private val stack: StackComponent,
    private val details: DetailsComponent,
    private val launcher: PlatformFeatureLauncher
) : AppNavigator {

    override fun back() = if (details.isOpen) details.back() else stack.back()
    private fun navigate(destination: Destination) = when {
        !isWideScreen -> stack.navigate(destination)
        else -> details.navigate(destination)
    }

    override fun info() = stack.navigate(Destination.Info)
    override fun home() = stack.navigate(Destination.Home)
    override fun xmlActivity() = launcher.xmlActivity()
    override fun simpleFeature() = navigate(Destination.SimpleFeature)
    override fun lceFeature() = navigate(Destination.LCEFeature)
    override fun savedStateFeature() = navigate(Destination.SavedState)
    override fun diConfigFeature() = navigate(Destination.DiConfig)
    override fun loggingFeature() = navigate(Destination.Logging)
    override fun undoRedoFeature() = navigate(Destination.UndoRedo)
    override fun progressiveFeature() = navigate(Destination.Progressive)
    override fun stateTransactionsFeature() = navigate(Destination.StateTransactions)
    override fun decomposeFeature() = navigate(Destination.Decompose)

    @Composable
    override fun rememberBackNavigationState(): State<Boolean> {
        val stackState by stack.rememberBackNavigationState()
        val detailsState by details.rememberBackNavigationState()
        return remember { derivedStateOf { stackState || detailsState } }
    }
}
