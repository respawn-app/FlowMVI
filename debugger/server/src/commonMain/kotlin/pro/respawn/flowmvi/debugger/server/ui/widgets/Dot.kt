package pro.respawn.flowmvi.debugger.server.ui.widgets

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pro.respawn.flowmvi.debugger.server.ui.theme.Elevation
import pro.respawn.flowmvi.debugger.server.ui.theme.RespawnTheme

@Composable
fun Dot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 8.dp,
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = color,
        tonalElevation = Elevation.small,
        shadowElevation = Elevation.small,
        content = {},
    )
}

@Composable
@Preview
private fun DotPreview() = RespawnTheme {
    Dot(color = MaterialTheme.colorScheme.error)
}
