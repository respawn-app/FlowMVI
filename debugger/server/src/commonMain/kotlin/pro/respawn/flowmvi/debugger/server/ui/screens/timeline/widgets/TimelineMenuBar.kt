package pro.respawn.flowmvi.debugger.server.ui.screens.timeline.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.EventType
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineState

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun IntentReceiver<TimelineIntent>.TimelineMenuBar(
    state: TimelineState.DisplayingTimeline,
    modifier: Modifier = Modifier
) {
    FlowRow(verticalArrangement = Arrangement.Center, modifier = modifier) {
        IconButton(
            onClick = { intent(TimelineIntent.StopServerClicked) },
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(Icons.AutoMirrored.Rounded.ExitToApp, contentDescription = null)
        }
        OutlinedButton(
            onClick = { intent(TimelineIntent.AutoScrollToggled) },
            modifier = Modifier.padding(8.dp),
        ) {
            AnimatedVisibility(state.autoScroll) {
                Icon(Icons.Rounded.Done, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            }
            Text("Autoscroll")
        }
        StoreSelectorDropDown(state.stores, state.filters, modifier = Modifier.padding(8.dp))
        EventType.entries.forEach {
            FilterChip(
                selected = it in state.filters.events,
                onClick = { intent(TimelineIntent.EventFilterSelected(it)) },
                modifier = Modifier.padding(8.dp),
                label = { Text(it.name) },
            )
        }
    }
}
