package pro.respawn.flowmvi.debugger.server.navigation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.androidPredictiveBackAnimatable
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import pro.respawn.flowmvi.debugger.server.navigation.component.DestinationComponent
import pro.respawn.flowmvi.debugger.server.navigation.component.RootComponent
import pro.respawn.flowmvi.debugger.server.navigation.destination.Destination
import pro.respawn.flowmvi.debugger.server.navigation.util.NavigationDefaults.DefaultNavAnimation
import pro.respawn.flowmvi.debugger.server.navigation.util.NavigationDefaults.NavAnimSpec

@OptIn(ExperimentalDecomposeApi::class)
@Composable
internal fun <C : Destination, T : DestinationComponent> defaultNavAnimation(
    component: RootComponent
) = remember(component) {
    predictiveBackAnimation<C, T>(
        backHandler = component.backHandler,
        onBack = component::back,
        fallbackAnimation = stackAnimation { (destination, _) ->
            if (destination.topLevel) fade(NavAnimSpec) else DefaultNavAnimation
        },
        selector = { initialEvent, _, _ -> androidPredictiveBackAnimatable(initialEvent) },
    )
}
