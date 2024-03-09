package pro.respawn.flowmvi.debugger.server.ui.screens.timeline

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.api.SubscriptionMode
import pro.respawn.flowmvi.compose.dsl.DefaultLifecycle
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineAction.CopyToClipboard
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineAction.ScrollToItem
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.EntryClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.HostChanged
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.PortChanged
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.StartServerClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineState.DisplayingTimeline
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.widgets.FocusedEventLayout
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.widgets.StoreEventItem
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.widgets.TimelineMenuBar
import pro.respawn.flowmvi.debugger.server.ui.widgets.DynamicTwoPaneLayout
import pro.respawn.flowmvi.debugger.server.ui.widgets.RTextInput
import pro.respawn.flowmvi.server.generated.resources.Res
import pro.respawn.flowmvi.server.generated.resources.icon_192
import java.time.format.DateTimeFormatter

/**
 * The Timeline (Main) screen of the debugger.
 */
@Composable
fun TimelineScreen() {
    val scope = rememberCoroutineScope()
    val store = remember { timelineStore(scope) }
    val listState = rememberLazyListState()
    val clipboard = LocalClipboardManager.current
    with(store) {
        val state by subscribe(DefaultLifecycle, SubscriptionMode.Visible) {
            when (it) {
                is ScrollToItem -> listState.animateScrollToItem(it.index)
                is CopyToClipboard -> clipboard.setText(AnnotatedString(it.text))
            }
        }
        Scaffold {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(it)) {
                TimelineScreenContent(state, listState)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalResourceApi::class)
@Composable
private fun IntentReceiver<TimelineIntent>.TimelineScreenContent(
    state: TimelineState,
    listState: LazyListState,
) {
    val timestampFormatter = remember { DateTimeFormatter.ISO_DATE_TIME }
    when (state) {
        is TimelineState.ConfiguringServer -> Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(Res.drawable.icon_192),
                modifier = Modifier.padding(64.dp).size(120.dp),
                contentDescription = null,
            )
            RTextInput(state.host, onTextChange = { intent(HostChanged(it)) }, label = "Host")
            RTextInput(state.port, onTextChange = { intent(PortChanged(it)) }, label = "Port")
            TextButton(onClick = { intent(StartServerClicked) }, enabled = state.canStart) { Text("Connect") }
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
            TimelineMenuBar(state)

            DynamicTwoPaneLayout(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                secondPaneVisible = state.focusedEvent != null,
                firstPaneContent = {
                    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                        items(state.currentEvents) {
                            StoreEventItem(
                                event = it,
                                onClick = { intent(EntryClicked(it)) },
                                format = timestampFormatter::format,
                                modifier = Modifier.animateItemPlacement(),
                                selected = it.event == state.focusedEvent?.event,
                            )
                        }
                        if (state.currentEvents.isEmpty()) item {
                            WaitingForEventsLayout(Modifier.fillParentMaxSize())
                        }
                    }
                },
                secondaryPaneContent = {
                    Row(Modifier.padding(12.dp)) {
                        Column(modifier = Modifier.fillMaxWidth()) inner@{
                            if (state.focusedEvent == null) return@inner
                            FocusedEventLayout(state.focusedEvent, timestampFormatter::format)
                        }
                    }
                }
            )
        } // column
    } // when
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
