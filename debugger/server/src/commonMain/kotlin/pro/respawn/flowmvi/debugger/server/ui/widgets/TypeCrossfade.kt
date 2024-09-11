package pro.respawn.flowmvi.debugger.server.ui.widgets

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * TypeCrossfade is a [Crossfade] variation that runs a fade-through animation when the type of the [state] [T] changes.
 * It will not run the animation when the object itself changes.
 *
 * This should be used for any page where multiple states are defined to transition between them.
 * This can have a small performance impact though, so avoid using this where the type changes very frequently.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
inline fun <reified T : Any> TypeCrossfade(
    state: T,
    modifier: Modifier = Modifier,
    fill: Boolean = true,
    alignment: Alignment = Alignment.Center,
    animationSpec: FiniteAnimationSpec<Float> = tween(),
    crossinline content: @Composable T.() -> Unit
) {
    val transition = updateTransition(targetState = state, label = "TypeCrossfade")
    transition.Crossfade(
        contentKey = { it::class },
        animationSpec = animationSpec,
        modifier = modifier,
    ) {
        Box(contentAlignment = alignment, modifier = Modifier.then(if (fill) Modifier.fillMaxSize() else Modifier)) {
            content(it)
        }
    }
}
