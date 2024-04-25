package pro.respawn.flowmvi.sample.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pro.respawn.flowmvi.sample.ui.theme.Opacity
import pro.respawn.flowmvi.sample.ui.theme.rememberRandomColor
import pro.respawn.flowmvi.sample.util.adaptiveWidth
import pro.respawn.flowmvi.sample.util.noIndicationClickable
import pro.respawn.kmmutils.common.takeIfValid
import kotlin.contracts.contract

// TODO: Fix for a kotlin compiler bug
fun takeIf(condition: Boolean, block: @Composable () -> Unit): (@Composable () -> Unit)? {
    contract {
        returns() implies condition
    }
    return if (condition) block else null
}

@Composable
fun RMenuItem(
    title: (@Composable () -> Unit),
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    secondary: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .animateContentSize()
            .adaptiveWidth()
            .then(if (onClick != null) Modifier.noIndicationClickable(onClick = onClick) else Modifier),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            visible = icon != null,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            icon?.let { it() }
        }

        Column(modifier = Modifier.weight(1f, true), verticalArrangement = Arrangement.Center) {
            Box(Modifier.padding(2.dp)) {
                title()
            }
            AnimatedVisibility(secondary != null) {
                Box(modifier = Modifier.padding(2.dp)) {
                    secondary?.let { it() }
                }
            }
        }

        AnimatedVisibility(
            visible = trailing != null,
            modifier = Modifier.padding(2.dp),
        ) {
            trailing?.let { it() }
        }
    }
}

@Composable
fun RMenuItem(
    icon: ImageVector?,
    color: Color,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    secondary: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) = RMenuItem(
    icon = icon?.let { { RMenuItemIcon(it, color) } },
    title = title,
    modifier = modifier,
    onClick = onClick,
    secondary = secondary,
    trailing = trailing,
)

@Composable
fun RMenuItem(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = rememberRandomColor(),
    icon: ImageVector? = null,
    enabled: Boolean = true,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val textColor = LocalContentColor.current.copy(alpha = if (enabled) Opacity.enabled else Opacity.disabled)
    RMenuItem(
        icon = icon?.let { { RMenuItemIcon(icon = it, color = if (enabled) color else LocalContentColor.current) } },
        title = {
            RMenuItemTitle(
                title,
                color = textColor,
            )
        },
        modifier = modifier,
        onClick = onClick.takeIf { enabled },
        secondary = subtitle.takeIfValid()?.let { { RMenuItemSubtitle(it, color = textColor) } },
        trailing = trailing,
    )
}

@Composable
fun RMenuItemIcon(
    icon: ImageVector,
    color: Color,
) = RCircleIcon(
    icon = icon,
    color = color,
    size = 40.dp,
)

@Composable
fun RMenuItemSubtitle(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    val animatedColor by animateColorAsState(color)
    Text(
        modifier = modifier
            .padding(2.dp)
            .animateContentSize(),
        text = text,
        maxLines = 3,
        color = animatedColor,
        style = MaterialTheme.typography.bodySmall,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun RMenuItemTitle(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    val animatedColor by animateColorAsState(color)
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        maxLines = 3,
        color = animatedColor,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .padding(2.dp)
            .animateContentSize(),
    )
}
