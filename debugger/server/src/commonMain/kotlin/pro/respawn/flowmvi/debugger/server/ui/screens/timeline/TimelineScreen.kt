package pro.respawn.flowmvi.debugger.server.ui.screens.timeline

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.requireLifecycle
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.debugger.server.di.container
import pro.respawn.flowmvi.debugger.server.navigation.AppNavigator
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineAction.CopyToClipboard
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineAction.GoToConnect
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineAction.GoToStoreDetails
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineAction.ScrollToItem
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.CloseFocusedEventClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.CopyEventClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.EventClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineState.DisplayingTimeline
import pro.respawn.flowmvi.debugger.server.ui.widgets.RErrorView
import pro.respawn.flowmvi.debugger.server.ui.widgets.RScaffold
import pro.respawn.flowmvi.debugger.server.ui.widgets.StoreEventListDetailsLayout
import pro.respawn.flowmvi.debugger.server.ui.widgets.TypeCrossfade
import pro.respawn.kmmutils.compose.annotate

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
            is CopyToClipboard -> clipboard.setText(it.text.annotate(SpanStyle(fontFamily = FontFamily.Monospace)))
            is GoToConnect -> navigator.connect()
            is GoToStoreDetails -> navigator.storeDetails(it.storeId)
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
    when (this) {
        is TimelineState.Loading -> CircularProgressIndicator()
        is TimelineState.Error -> RErrorView(e)
        is DisplayingTimeline -> Column {
            TimelineMenuBar(this@TypeCrossfade)
            StoreEventListDetailsLayout(
                events = currentEvents,
                focusedEvent = focusedEvent,
                listState = listState,
                onCopy = { intent(CopyEventClicked) },
                onClose = { intent(CloseFocusedEventClicked) },
                onClick = { intent(EventClicked(it)) },
                modifier = Modifier.fillMaxSize().padding(8.dp)
            )
        } // column
    } // when
}
