package pro.respawn.flowmvi.debugger.server.ui.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDateTime
import pro.respawn.flowmvi.debugger.server.ServerEventEntry

@Composable
internal fun StoreEventList(
    events: ImmutableList<ServerEventEntry>,
    isSelected: (ServerEventEntry) -> Boolean,
    onClick: (ServerEventEntry) -> Unit,
    formatTimestamp: (LocalDateTime) -> String,
    listState: LazyListState = rememberLazyListState(),
) = LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
    items(events, key = { it.id }) {
        StoreEventItem(
            event = it,
            onClick = { onClick(it) },
            format = formatTimestamp,
            modifier = Modifier.animateItem(),
            selected = isSelected(it),
        )
    }
    if (events.isEmpty()) item {
        WaitingForEventsLayout(Modifier.fillParentMaxSize())
    }
}

@Composable
private fun WaitingForEventsLayout(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Waiting for events", modifier = Modifier.padding(top = 12.dp))
            CircularProgressIndicator(modifier = Modifier.padding(12.dp))
        }
    }
}
