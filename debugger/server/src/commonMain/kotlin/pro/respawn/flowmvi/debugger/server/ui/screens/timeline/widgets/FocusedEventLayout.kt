package pro.respawn.flowmvi.debugger.server.ui.screens.timeline.widgets

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.toJavaLocalDateTime
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.debugger.server.ui.representation
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.FocusedEvent
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.CloseFocusedEntryClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.CopyEventClicked
import pro.respawn.flowmvi.debugger.server.ui.theme.Opacity
import pro.respawn.flowmvi.debugger.server.ui.type
import java.time.LocalDateTime

@Composable
internal fun IntentReceiver<TimelineIntent>.FocusedEventLayout(
    event: FocusedEvent,
    format: LocalDateTime.() -> String,
    modifier: Modifier = Modifier
) {
    val timestamp = remember { format(event.timestamp.toJavaLocalDateTime()) }
    Crossfade(event) { value ->
        SelectionContainer(modifier = modifier) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = buildAnnotatedString {
                                append("${value.event.type} in ")
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                    append(value.storeName)
                                }
                            },
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(end = 8.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = timestamp,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .alpha(Opacity.secondary)
                        )
                    }
                    Row {
                        val colors = IconButtonDefaults.filledIconButtonColors(MaterialTheme.colorScheme.surfaceVariant)
                        IconButton(
                            onClick = { intent(CopyEventClicked) },
                            colors = colors,
                        ) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                        }
                        IconButton(
                            onClick = { intent(CloseFocusedEntryClicked) },
                            colors = colors,
                        ) { Icon(Icons.Rounded.Close, contentDescription = null) }
                    }
                }
                Divider()
                Column(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                    Text(value.event.representation, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                }
            }
        }
    }
}
