package pro.respawn.flowmvi.debugger.server.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import pro.respawn.flowmvi.debugger.server.ServerEventEntry
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.FocusedEvent
import pro.respawn.flowmvi.debugger.server.ui.util.TimestampFormatter

// TODO: To avoid duplication, we can make a container for this

@Composable
internal fun StoreEventListDetailsLayout(
    events: ImmutableList<ServerEventEntry>,
    focusedEvent: FocusedEvent?,
    onClick: (ServerEventEntry) -> Unit,
    onCopy: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) = DynamicTwoPaneLayout(
    modifier = modifier,
    secondPaneVisible = focusedEvent != null,
    firstPaneContent = {
        StoreEventList(
            events = events,
            isSelected = { it == focusedEvent?.event },
            onClick = onClick,
            formatTimestamp = TimestampFormatter,
            listState = listState,
        )
    },
    secondaryPaneContent = {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) inner@{
            if (focusedEvent == null) return@inner
            FocusedEventLayout(
                event = focusedEvent,
                onCopy = onCopy,
                onClose = onClose,
                format = TimestampFormatter
            )
        }
    }
)
