package pro.respawn.flowmvi.debugger.server.ui.screens.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.debugger.server.ServerEventEntry
import pro.respawn.flowmvi.debugger.server.ui.representation
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineAction.ScrollToItem
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.AutoScrollToggled
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.CloseFocusedEntryClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.EntryClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.EventFilterSelected
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.HostChanged
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.PortChanged
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.StartServerClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.StopServerClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineState.DisplayingTimeline
import pro.respawn.flowmvi.debugger.server.ui.theme.Opacity
import pro.respawn.flowmvi.debugger.server.ui.type
import pro.respawn.flowmvi.debugger.server.ui.widgets.DropDownActions
import pro.respawn.flowmvi.debugger.server.ui.widgets.RDropDownMenu
import pro.respawn.flowmvi.debugger.server.ui.widgets.RTextInput
import pro.respawn.flowmvi.debugger.server.ui.widgets.rememberDropDownActions
import java.time.format.DateTimeFormatter

/**
 * The Timeline (Main) screen of the debugger.
 */
@Composable
fun TimelineScreen() {
    val scope = rememberCoroutineScope()
    val store = remember { timelineStore(scope) }
    val listState = rememberLazyListState()
    with(store) {
        val state by subscribe {
            when (it) {
                is ScrollToItem -> listState.animateScrollToItem(it.index)
            }
        }
        Scaffold {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(it)) {
                TimelineScreenContent(state, listState)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun IntentReceiver<TimelineIntent>.TimelineScreenContent(
    state: TimelineState,
    listState: LazyListState
) {
    val timestampFormatter = remember { DateTimeFormatter.ISO_DATE_TIME }
    when (state) {
        is TimelineState.ConfiguringServer -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RTextInput(state.host, onTextChange = { intent(HostChanged(it)) }, label = "Host")
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
        is DisplayingTimeline -> Column {
            FlowRow(verticalArrangement = Arrangement.Center) {
                IconButton(
                    onClick = { intent(StopServerClicked) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(Icons.Rounded.ExitToApp, contentDescription = null)
                }
                OutlinedButton(
                    onClick = { intent(AutoScrollToggled) },
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
                        onClick = { intent(EventFilterSelected(it)) },
                        modifier = Modifier.padding(8.dp),
                        label = { Text(it.name) },
                    )
                }
            }
            Row(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.weight(1f)) {
                    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                        items(state.currentEvents) {
                            StoreEventItem(
                                event = it,
                                onClick = { intent(EntryClicked(it)) },
                                format = timestampFormatter::format
                            )
                        }
                        if (state.currentEvents.isEmpty()) item {
                            Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Waiting for events", modifier = Modifier.padding(top = 12.dp))
                                    CircularProgressIndicator(modifier = Modifier.padding(12.dp))
                                }
                            }
                        }
                    }
                }
                AnimatedVisibility(
                    visible = state.focusedEvent != null,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                ) {
                    Row {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .width(3.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(Opacity.disabled))
                                .fillMaxHeight()
                        )
                        Column(modifier = Modifier.fillMaxWidth()) inner@{
                            FocusedEventLayout(state.focusedEvent ?: return@inner, timestampFormatter::format)
                        }
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
        var expanded by remember { mutableStateOf(false) }
        RDropDownMenu(
            expanded = expanded,
            onExpand = { expanded = !expanded },
            button = {
                OutlinedButton(onClick = { expanded = !expanded }) {
                    Text(filters.store?.name ?: "All stores")
                }
            },
            actions = rememberDropDownActions(stores) {
                buildList {
                    add(
                        DropDownActions.Action("All stores") {
                            intent(TimelineIntent.StoreFilterSelected(null))
                        }
                    )
                    stores.forEach {
                        add(
                            DropDownActions.Action(text = it.name) {
                                intent(TimelineIntent.StoreFilterSelected(it))
                            }
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun StoreEventItem(
    event: ServerEventEntry,
    onClick: () -> Unit,
    format: java.time.LocalDateTime.() -> String,
    modifier: Modifier = Modifier,
) {
    val timestamp = remember(event.timestamp) {
        format(event.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime())
    }
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        supportingContent = {
            Text(
                timestamp,
                style = MaterialTheme.typography.labelSmall,
                overflow = TextOverflow.Ellipsis
            )
        },
        headlineContent = {
            SelectionContainer {
                Text("${event.name} <- ${event.event.type}", overflow = TextOverflow.Ellipsis)
            }
        },
    )
}

@Composable
private fun IntentReceiver<TimelineIntent>.FocusedEventLayout(
    event: FocusedEvent,
    format: java.time.LocalDateTime.() -> String,
    modifier: Modifier = Modifier
) {
    val timestamp = remember { format(event.timestamp.toJavaLocalDateTime()) }
    Crossfade(event) { value ->
        SelectionContainer(modifier = modifier) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = buildAnnotatedString {
                                append("${value.event.type} in ")
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                    append(value.storeName)
                                }
                            },
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = timestamp,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(end = 8.dp).alpha(Opacity.secondary)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = { intent(CloseFocusedEntryClicked) },
                        colors = IconButtonDefaults.filledIconButtonColors(MaterialTheme.colorScheme.surfaceVariant)
                    ) { Icon(Icons.Rounded.Close, contentDescription = null) }
                }
                Divider()
                Column(modifier = Modifier.horizontalScroll(rememberScrollState()).fillMaxWidth()) {
                    Divider()
                    Text(value.event.representation, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
@Preview
private fun TimelineScreenPreview() {
    TimelineScreen()
}
