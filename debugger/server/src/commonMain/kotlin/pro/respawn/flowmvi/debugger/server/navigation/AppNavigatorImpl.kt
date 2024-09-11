package pro.respawn.flowmvi.debugger.server.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import pro.respawn.flowmvi.debugger.server.navigation.component.RootComponent
import pro.respawn.flowmvi.debugger.server.navigation.component.StackComponent
import pro.respawn.flowmvi.debugger.server.navigation.destination.Destination
import pro.respawn.flowmvi.debugger.server.navigation.details.DetailsComponent

@Composable
fun rememberAppNavigator(
    isWideScreen: Boolean,
    root: RootComponent
): AppNavigator = remember(isWideScreen, root) {
    AppNavigatorImpl(isWideScreen, root, root.details)
}

private class AppNavigatorImpl(
    val isWideScreen: Boolean,
    private val stack: StackComponent,
    private val details: DetailsComponent,
) : AppNavigator {

    private val currentDestination get() = stack.stack.value.active.configuration

    override fun back() = if (details.isOpen) details.back() else stack.back()

    private fun navigate(destination: Destination) = when {
        isWideScreen && destination detailsOf currentDestination -> details.navigate(destination)
        else -> {
            details.back()
            stack.navigate(destination)
        }
    }

    override fun timeline() = navigate(Destination.Timeline)
    override fun connect() = navigate(Destination.Connect)

    @Composable
    override fun rememberBackNavigationState(): State<Boolean> {
        val stackState by stack.rememberBackNavigationState()
        val detailsState by details.rememberBackNavigationState()
        return remember { derivedStateOf { stackState || detailsState } }
    }
}
