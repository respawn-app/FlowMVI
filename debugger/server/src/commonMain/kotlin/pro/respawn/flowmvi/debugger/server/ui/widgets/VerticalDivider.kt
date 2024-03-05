package pro.respawn.flowmvi.debugger.server.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pro.respawn.flowmvi.debugger.server.ui.theme.Opacity

@Composable
internal fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(3.dp)
            .background(MaterialTheme.colorScheme.surface.copy(Opacity.disabled))
            .fillMaxHeight()
    )
}
