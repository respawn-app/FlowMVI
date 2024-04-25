package pro.respawn.flowmvi.sample.navigation.util

import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale

internal object NavigationDefaults {

    val WideScreenWidth = 1200.dp
    const val NavAnimDuration = 300
    val NavAnimSpec = tween<Float>(NavAnimDuration)
    val DefaultNavAnimation = scale(NavAnimSpec, frontFactor = 0.85f, backFactor = 1.15f) + fade(NavAnimSpec)
}
