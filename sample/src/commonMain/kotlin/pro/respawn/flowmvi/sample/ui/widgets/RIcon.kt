package pro.respawn.flowmvi.sample.ui.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import pro.respawn.flowmvi.sample.ui.theme.Opacity
import pro.respawn.flowmvi.sample.ui.theme.Size

@Composable
fun RIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = Size.icon,
    color: Color = Color.Unspecified,
    alpha: Float = Opacity.enabled,
) {
    Image(
        imageVector = icon,
        modifier = modifier.requiredSizeIn(maxWidth = size, maxHeight = size).testTag(icon.name),
        contentDescription = null,
        alpha = alpha,
        colorFilter = ColorFilter.tint(color.takeOrElse { LocalContentColor.current }),
    )
}

@Composable
fun RIcon(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = Size.icon,
    color: Color = Color.Unspecified,
    enabled: Boolean = true,
    enabledAlpha: Float = 1f,
) {
    val alpha by animateFloatAsState(if (enabled) enabledAlpha else Opacity.disabled)
    Box(
        modifier = modifier
            .clickable(
                onClick = onClick,
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(radius = size / 2),
            )
            .minimumInteractiveComponentSize(),
        contentAlignment = Alignment.Center,
    ) {
        RIcon(
            icon = icon,
            size = size,
            color = color,
            alpha = alpha,
        )
    }
}
