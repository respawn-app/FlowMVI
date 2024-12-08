package pro.respawn.flowmvi.debugger.server.ui.screens.storedetails

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
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
import pro.respawn.flowmvi.debugger.server.di.container
import pro.respawn.flowmvi.debugger.server.navigation.AppNavigator
import pro.respawn.flowmvi.debugger.server.navigation.util.backNavigator
import pro.respawn.flowmvi.debugger.server.ui.screens.storedetails.StoreDetailsAction.CopyToClipboard
import pro.respawn.flowmvi.debugger.server.ui.screens.storedetails.StoreDetailsIntent.CloseFocusedEventClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.storedetails.StoreDetailsIntent.CopyEventClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.storedetails.StoreDetailsIntent.EventClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.storedetails.StoreDetailsState.DisplayingStore
import pro.respawn.flowmvi.debugger.server.ui.theme.RespawnTheme
import pro.respawn.flowmvi.debugger.server.ui.widgets.RErrorView
import pro.respawn.flowmvi.debugger.server.ui.widgets.RScaffold
import pro.respawn.flowmvi.debugger.server.ui.widgets.StoreEventListDetailsLayout
import pro.respawn.flowmvi.debugger.server.ui.widgets.TypeCrossfade
import pro.respawn.flowmvi.util.typed
import pro.respawn.kmmutils.common.copies
import pro.respawn.kmmutils.compose.annotate
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailsScreen(
    storeId: Uuid,
    navigator: AppNavigator,
) = with(container<StoreDetailsContainer, _, _, _> { parametersOf(storeId) }) {
    val clipboard = LocalClipboardManager.current

    val state by subscribe(requireLifecycle()) {
        when (it) {
            is CopyToClipboard -> clipboard.setText(it.text.annotate(SpanStyle(fontFamily = FontFamily.Monospace)))
        }
    }

    RScaffold(
        title = state.typed<DisplayingStore>()?.let { it.name ?: it.id.toString() },
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
                overflow = FlowRowOverflow.Visible,
            ) {
                StoreCommand.entries.forEach {
                    OutlinedButton(
                        onClick = { intent(StoreDetailsIntent.SendCommandClicked(event = it)) },
                        modifier = Modifier.padding(8.dp),
                    ) { Text(text = it.label) }
                }
            }
            Spacer(Modifier.height(12.dp))
            StoreEventListDetailsLayout(
                events = eventLog,
                focusedEvent = focusedEvent,
                onCopy = { intent(CopyEventClicked) },
                onClose = { intent(CloseFocusedEventClicked) },
                onClick = { intent(EventClicked(it)) },
                modifier = Modifier.fillMaxSize().padding(8.dp)
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
                eventLog = ServerEventEntry(
                    storeId = Uuid.random(),
                    name = "Store",
                    event = ClientEvent.StoreConnected("Store", id = Uuid.random())
                ).copies(10).toImmutableList(),
            ),
        )
    }
}
