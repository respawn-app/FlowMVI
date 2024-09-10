package pro.respawn.flowmvi.debugger.server.ui.screens.timeline

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.DefaultLifecycle
import pro.respawn.flowmvi.compose.dsl.requireLifecycle
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.debugger.server.di.container
import pro.respawn.flowmvi.debugger.server.navigation.AppNavigator
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineAction.CopyToClipboard
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineAction.ScrollToItem
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.EntryClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineState.DisplayingTimeline
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.widgets.FocusedEventLayout
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.widgets.StoreEventItem
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.widgets.TimelineMenuBar
import pro.respawn.flowmvi.debugger.server.ui.widgets.DynamicTwoPaneLayout
import pro.respawn.flowmvi.debugger.server.ui.widgets.RErrorView
import pro.respawn.flowmvi.debugger.server.ui.widgets.RScaffold
import pro.respawn.flowmvi.debugger.server.ui.widgets.TypeCrossfade
import java.time.format.DateTimeFormatter

/**
 * The Timeline (Main) screen of the debugger.
 */
@Composable
fun TimelineScreen(
    navigator: AppNavigator,
) = with(container<TimelineContainer, _, _, _>()) {
    val listState = rememberLazyListState()
    val clipboard = LocalClipboardManager.current
    val state by subscribe(requireLifecycle()) {
        when (it) {
            is ScrollToItem -> listState.animateScrollToItem(it.index)
            is CopyToClipboard -> clipboard.setText(AnnotatedString(it.text))
            is TimelineAction.GoToConnect -> navigator.connect()
        }
    }
    RScaffold {
        TimelineScreenContent(state, listState)
    }
}

@Composable
private fun IntentReceiver<TimelineIntent>.TimelineScreenContent(
    state: TimelineState,
    listState: LazyListState,
) = TypeCrossfade(state) {
    val timestampFormatter = remember { DateTimeFormatter.ISO_DATE_TIME }
    when (this) {
        is TimelineState.Loading -> CircularProgressIndicator()
        is TimelineState.Error -> RErrorView(e)
        is DisplayingTimeline -> Column {
            TimelineMenuBar(this@TypeCrossfade)
            DynamicTwoPaneLayout(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                secondPaneVisible = focusedEvent != null,
                firstPaneContent = {
                    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                        items(currentEvents) {
                            StoreEventItem(
                                event = it,
                                onClick = { intent(EntryClicked(it)) },
                                format = timestampFormatter::format,
                                modifier = Modifier.animateItem(),
                                selected = it.event == focusedEvent?.event,
                            )
                        }
                        if (currentEvents.isEmpty()) item {
                            WaitingForEventsLayout(Modifier.fillParentMaxSize())
                        }
                    }
                },
                secondaryPaneContent = {
                    Row(Modifier.padding(12.dp)) {
                        Column(modifier = Modifier.fillMaxWidth()) inner@{
                            if (focusedEvent == null) return@inner
                            FocusedEventLayout(focusedEvent, timestampFormatter::format)
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
