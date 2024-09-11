package pro.respawn.flowmvi.debugger.server.ui.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import pro.respawn.flowmvi.debugger.server.ServerEventEntry
import pro.respawn.flowmvi.debugger.server.ui.theme.Opacity
import pro.respawn.flowmvi.debugger.server.util.type

@Composable
internal fun StoreEventItem(
    event: ServerEventEntry,
    selected: Boolean,
    onClick: () -> Unit,
    format: (LocalDateTime) -> String,
    modifier: Modifier = Modifier,
) {
    val timestamp = remember(event.timestamp) {
        format(event.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()))
    }
    val color by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = Opacity.tint) else Color.Transparent
    )
    ListItem(
        modifier = modifier.clickable(onClick = onClick).clip(MaterialTheme.shapes.extraSmall),
        colors = ListItemDefaults.colors(containerColor = color),
        supportingContent = {
            Text(
                timestamp,
                style = MaterialTheme.typography.labelSmall,
                overflow = TextOverflow.Ellipsis
            )
        },
        headlineContent = {
            SelectionContainer {
                Text(
                    text = "${event.name} <- ${event.event.type}",
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        },
    )
}
