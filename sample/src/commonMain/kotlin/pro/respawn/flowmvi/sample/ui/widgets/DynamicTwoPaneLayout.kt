package pro.respawn.flowmvi.sample.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import pro.respawn.flowmvi.sample.navigation.util.NavigationDefaults
import pro.respawn.kmmutils.common.midpoint
import pro.respawn.kmmutils.common.takeIfNotZero

@Composable
internal fun DynamicTwoPaneLayout(
    secondPaneVisible: Boolean,
    firstPaneContent: @Composable () -> Unit,
    secondaryPaneContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    widthRange: ClosedFloatingPointRange<Float> = 0.25f..0.75f
) {
    var contentWidth by remember { mutableStateOf(0f) }
    var paneWidth by remember { mutableStateOf(widthRange.midpoint) }
    val animatedPaneWidth by animateFloatAsState(
        targetValue = paneWidth.takeIf { secondPaneVisible } ?: 0f,
        animationSpec = tween(easing = EaseOutCubic, durationMillis = NavigationDefaults.NavAnimDuration)
    )
    val draggableState = rememberDraggableState {
        paneWidth = (paneWidth + it / (contentWidth.takeIfNotZero() ?: 1f)).coerceIn(widthRange)
    }
    Row(modifier = modifier.onSizeChanged { contentWidth = it.width.toFloat() }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        ) {
            firstPaneContent()
        }
        AnimatedVisibility(
            visible = secondPaneVisible,
            modifier = Modifier.draggable(draggableState, Orientation.Horizontal, reverseDirection = true)
        ) {
            VerticalDivider(Modifier.padding(horizontal = 12.dp))
        }
        AnimatedVisibility(
            visible = animatedPaneWidth > 0,
            modifier = Modifier.fillMaxWidth(animatedPaneWidth),
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxSize(),
            ) {
                secondaryPaneContent()
            }
        }
    }
}
