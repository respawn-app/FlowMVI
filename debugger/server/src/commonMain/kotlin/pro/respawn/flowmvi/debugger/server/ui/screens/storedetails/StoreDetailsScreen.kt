package pro.respawn.flowmvi.debugger.server.ui.screens.storedetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import org.koin.core.parameter.parametersOf
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.requireLifecycle
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.compose.preview.EmptyReceiver
import pro.respawn.flowmvi.debugger.model.ClientEvent
import pro.respawn.flowmvi.debugger.server.ServerEventEntry
import pro.respawn.flowmvi.debugger.server.StoreCommand
import pro.respawn.flowmvi.debugger.server.StoreKey
import pro.respawn.flowmvi.debugger.server.di.container
import pro.respawn.flowmvi.debugger.server.navigation.AppNavigator
import pro.respawn.flowmvi.debugger.server.navigation.util.backNavigator
import pro.respawn.flowmvi.debugger.server.ui.screens.storedetails.StoreDetailsAction.CopyToClipboard
import pro.respawn.flowmvi.debugger.server.ui.screens.storedetails.StoreDetailsIntent.CloseFocusedEventClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.storedetails.StoreDetailsIntent.CopyEventClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.storedetails.StoreDetailsIntent.EventClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.storedetails.StoreDetailsState.DisplayingStore
import pro.respawn.flowmvi.debugger.server.ui.theme.RespawnTheme
import pro.respawn.flowmvi.debugger.server.ui.util.TimestampFormatter
import pro.respawn.flowmvi.debugger.server.ui.util.setText
import pro.respawn.flowmvi.debugger.server.ui.widgets.DynamicTwoPaneLayout
import pro.respawn.flowmvi.debugger.server.ui.widgets.FocusedEventLayout
import pro.respawn.flowmvi.debugger.server.ui.widgets.RErrorView
import pro.respawn.flowmvi.debugger.server.ui.widgets.RScaffold
import pro.respawn.flowmvi.debugger.server.ui.widgets.StoreEventList
import pro.respawn.flowmvi.debugger.server.ui.widgets.TypeCrossfade
import pro.respawn.flowmvi.util.typed
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailsScreen(
    key: StoreKey,
    navigator: AppNavigator,
) = with(container<StoreDetailsContainer, _, _, _> { parametersOf(key) }) {
    val clipboard = LocalClipboard.current

    val state by subscribe(requireLifecycle()) {
        when (it) {
            is CopyToClipboard -> clipboard.setText(it.text)
        }
    }

    RScaffold(
        title = state.typed<DisplayingStore>()?.title,
        onBack = navigator.backNavigator,
    ) {
        StoreDetailsScreenContent(state = state)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IntentReceiver<StoreDetailsIntent>.StoreDetailsScreenContent(
    state: StoreDetailsState,
) = TypeCrossfade(state) {
    when (this) {
        is StoreDetailsState.Error -> RErrorView(e)
        is StoreDetailsState.Loading -> CircularProgressIndicator()
        is StoreDetailsState.Disconnected -> Text(
            "Disconnected. Try to select another store or check the connection",
        )
        is DisplayingStore -> Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            ) {
                StoreCommand.entries.forEach {
                    OutlinedButton(
                        onClick = { intent(StoreDetailsIntent.SendCommandClicked(event = it)) },
                        modifier = Modifier.padding(8.dp),
                    ) { Text(text = it.label) }
                }
            }
            Spacer(Modifier.height(12.dp))
            DynamicTwoPaneLayout(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                secondPaneVisible = focusedEvent != null,
                firstPaneContent = {
                    StoreEventList(
                        events = eventLog,
                        isSelected = { it.id == focusedEvent?.id },
                        onClick = { intent(EventClicked(it)) },
                        formatTimestamp = TimestampFormatter,
                        listState = rememberLazyListState(),
                        entry = { it },
                        source = { key },
                    )
                },
                secondaryPaneContent = {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) inner@{
                        if (focusedEvent == null) return@inner
                        FocusedEventLayout(
                            event = focusedEvent,
                            onCopy = { intent(CopyEventClicked) },
                            onClose = { intent(CloseFocusedEventClicked) },
                            format = TimestampFormatter,
                        )
                    }
                }
            )
        }
    }
}

private val StoreCommand.label: String
    get() = when (this) {
        StoreCommand.Stop -> "Stop"
        StoreCommand.ResendIntent -> "Resend last intent"
        StoreCommand.RollbackState -> "Rollback state once"
        StoreCommand.ResendAction -> "Resend last action"
        StoreCommand.RethrowException -> "Rethrow last Exception"
        StoreCommand.SetInitialState -> "Reset to initial state"
    }

@Composable
@Preview
private fun StoreDetailsScreenPreview() = RespawnTheme {
    EmptyReceiver {
        StoreDetailsScreenContent(
            state = DisplayingStore(
                id = Uuid.random(),
                name = "Store ".repeat(10),
                connected = false,
                eventLog = List(10) {
                    ServerEventEntry(event = ClientEvent.StoreConnected("Store", id = Uuid.random()))
                }.toImmutableList(),
            ),
        )
    }
}
