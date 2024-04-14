package pro.respawn.flowmvi.sample.ui.widgets

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

const val DefaultProgressAnimationDurationMs = 800

@OptIn(ExperimentalTransitionApi::class)
@Composable
fun RProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    progressAnimationDuration: Int = DefaultProgressAnimationDurationMs,
    easing: Easing = EaseOut,
) {
    val transitionState = remember { MutableTransitionState(0f) }
    val transition = rememberTransition(transitionState, label = "progress")

    LaunchedEffect(progress) {
        transitionState.targetState = progress
    }

    val animatedProgress by transition.animateFloat(
        targetValueByState = { it },
        transitionSpec = { tween(durationMillis = progressAnimationDuration, easing = easing) },
        label = "progress",
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        strokeCap = StrokeCap.Round,
    )
}

@Composable
fun RProgressBar(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
) = LinearProgressIndicator(
    modifier = modifier,
    strokeCap = StrokeCap.Round,
    color = color,
    trackColor = trackColor,
)
