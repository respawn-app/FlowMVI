package pro.respawn.flowmvi.debugger.server.ui.screens.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.ListItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.debugger.server.ServerEventEntry
import pro.respawn.flowmvi.debugger.server.ui.representation
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.EventFilterSelected
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.HostChanged
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.PortChanged
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.StartServerClicked
import pro.respawn.flowmvi.debugger.server.ui.type
import pro.respawn.flowmvi.debugger.server.ui.widgets.DropDownActions
import pro.respawn.flowmvi.debugger.server.ui.widgets.RDropDownMenu
import pro.respawn.flowmvi.debugger.server.ui.widgets.RTextInput
import pro.respawn.flowmvi.debugger.server.ui.widgets.rememberDropDownActions
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun TimelineScreen() {
    val scope = rememberCoroutineScope()
    val store = remember { timelineStore(scope) }
    with(store) {
        val state by subscribe()
        Scaffold {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(it)) {
                TimelineScreenContent(state)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
private fun IntentReceiver<TimelineIntent>.TimelineScreenContent(state: TimelineState) {
    val timestampFormatter = remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG) }
    when (state) {
        is TimelineState.ConfiguringServer -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RTextInput(state.host, onTextChange = { intent(HostChanged(it)) }, label = "ClientHost")
                RTextInput(state.port, onTextChange = { intent(PortChanged(it)) }, label = "Port")
                TextButton(onClick = { intent(StartServerClicked) }, enabled = state.canStart) { Text("Connect") }
            }
        }
        is TimelineState.Error -> Column {
            Text("An error has occurred", fontSize = 32.sp)
            SelectionContainer {
                Text("Message: ${state.e.message}")
                Text("stack trace: ${state.e.stackTraceToString()}")
                // TODO: Report to github link
            }
        }
        is TimelineState.DisplayingTimeline -> Column {
            Row {
                FlowRow {
                    StoreSelectorDropDown(state.stores, state.filters)
                    EventType.entries.forEach {
                        FilterChip(
                            selected = it in state.filters.events,
                            onClick = { intent(EventFilterSelected(it)) },
                            modifier = Modifier.padding(12.dp),
                        ) { Text(it.name) }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.weight(1f)) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.currentEvents) {
                            StoreEventItem(it, timestampFormatter::format)
                        }
                        if (state.currentEvents.isEmpty()) item {
                            Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Started, waiting for events")
                            }
                        }
                    }
                }
                AnimatedVisibility(visible = state.focusedEvent != null) {
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        FocusedEventLayout(state.focusedEvent ?: return@AnimatedVisibility, timestampFormatter::format)
                    }
                }
            } // row
        } // column
    } // when
}

@Composable
private fun IntentReceiver<TimelineIntent>.StoreSelectorDropDown(
    stores: ImmutableList<StoreItem>,
    filters: TimelineFilters,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = stores.isNotEmpty(), modifier = modifier) {
        RDropDownMenu(
            button = {
                Surface {
                    Text(filters.store?.name ?: "All stores", modifier = Modifier.padding(8.dp))
                }
            },
            actions = rememberDropDownActions(stores) {
                stores.map {
                    DropDownActions.Action(text = it.representation) { intent(TimelineIntent.StoreFilterSelected(it)) }
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun StoreEventItem(
    event: ServerEventEntry,
    format: java.time.LocalDateTime.() -> String,
    modifier: Modifier = Modifier,
) {
    val timestamp = remember(event.timestamp) {
        format(event.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime())
    }
    ListItem(
        modifier = modifier,
        secondaryText = {
            Text(
                timestamp,
                style = MaterialTheme.typography.labelSmall,
                overflow = TextOverflow.Ellipsis
            )
        },
    ) {
        SelectionContainer {
            Text("${event.client.name} <- ${event.event.type}", overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun FocusedEventLayout(
    event: FocusedEvent,
    format: java.time.LocalDateTime.() -> String,
    modifier: Modifier = Modifier
) {
    val timestamp = remember { format(event.timestamp.toJavaLocalDateTime()) }
    SelectionContainer(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState()).fillMaxWidth()
        ) {
            Text("Type: ${event.event.type}")
            Text("From${event.storeName}")
            Text("At: $timestamp")
            Divider()
            Text(event.event.representation)
        }
    }
}

@Composable
@Preview
private fun TimelineScreenPreview() {
    TimelineScreen()
}
