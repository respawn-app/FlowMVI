package pro.respawn.flowmvi.debugger.server.ui.widgets

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import pro.respawn.flowmvi.debugger.server.ui.theme.Size

@Composable
fun RIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    size: Dp = Size.icon,
) = Icon(
    imageVector = icon,
    contentDescription = null,
    // because of a default size() modifier in icon sources, icons cannot be smaller than 24 dp.
    modifier = modifier.requiredSize(size),
    tint = color,
)

@Composable
fun RIcon(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = Color.Unspecified,
    size: Dp = Size.icon,
    interactionSource: MutableInteractionSource? = null,
) = IconButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    interactionSource = interactionSource,
    content = { RIcon(icon, color = color.takeOrElse { LocalContentColor.current }, size = size) },
)
