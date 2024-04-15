package pro.respawn.flowmvi.sample.navigation.util

import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.androidPredictiveBackAnimatable
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import pro.respawn.flowmvi.sample.navigation.component.DestinationComponent
import pro.respawn.flowmvi.sample.navigation.component.RootComponent
import pro.respawn.flowmvi.sample.navigation.destination.Destination
import pro.respawn.flowmvi.sample.navigation.util.NavAnimationDefaults.DefaultNavAnimation
import pro.respawn.flowmvi.sample.navigation.util.NavAnimationDefaults.NavAnimSpec

private object NavAnimationDefaults {

    const val NavAnimDuration = 300
    val NavAnimSpec = tween<Float>(NavAnimDuration)
    val DefaultNavAnimation = scale(NavAnimSpec, frontFactor = 0.85f, backFactor = 1.15f) + fade(NavAnimSpec)
}

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
