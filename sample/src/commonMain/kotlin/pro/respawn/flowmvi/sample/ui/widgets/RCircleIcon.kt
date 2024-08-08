package pro.respawn.flowmvi.sample.ui.widgets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pro.respawn.flowmvi.sample.ui.theme.Opacity
import pro.respawn.flowmvi.sample.ui.theme.rememberRandomColor

const val IconSizeMultiplier = 0.6f

@Composable
fun RCircleIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = rememberRandomColor(),
    elevation: Dp = 0.dp,
    size: Dp = 40.dp,
    backdropOpacity: Float = 0.17f,
    iconSize: Dp = size * IconSizeMultiplier,
) {
    val animatedColor by animateColorAsState(color)

    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = animatedColor.copy(alpha = backdropOpacity),
        contentColor = animatedColor,
        shadowElevation = elevation,
        tonalElevation = 0.dp,
        border = null,
        content = {
            AnimatedContent(icon) {
                RIcon(icon = it, color = animatedColor, size = iconSize)
            }
        },
    )
}

@Composable
fun RCircleIcon(
    icon: ImageVector,
    onClick: (() -> Unit),
    modifier: Modifier = Modifier,
    color: Color = rememberRandomColor(),
    elevation: Dp = 0.dp,
    size: Dp = 40.dp,
    backdropOpacity: Float = 0.17f,
    enabled: Boolean = true,
    iconSize: Dp = size * IconSizeMultiplier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) = Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = false, radius = size / 2),
            role = Role.Button,
            enabled = enabled,
            onClick = onClick,
        )
        .minimumInteractiveComponentSize()
) {
    RCircleIcon(
        icon = icon,
        color = if (enabled) color else MaterialTheme.colorScheme.onSurface.copy(Opacity.disabled),
        elevation = elevation,
        backdropOpacity = backdropOpacity,
        size = size,
        iconSize = iconSize,
    )
}
