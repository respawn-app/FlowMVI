package pro.respawn.flowmvi.debugger.server.ui.screens.storedetails

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.debugger.server.ServerEventEntry
import pro.respawn.flowmvi.debugger.server.StoreCommand
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.FocusedEvent
import kotlin.uuid.Uuid

@Immutable
internal sealed interface StoreDetailsState : MVIState {

    data object Loading : StoreDetailsState
    data object Disconnected : StoreDetailsState
    data class Error(val e: Exception) : StoreDetailsState
    data class DisplayingStore(
        val id: Uuid,
        val name: String?,
        val connected: Boolean,
        val eventLog: ImmutableList<ServerEventEntry>,
        val focusedEvent: FocusedEvent? = null,
    ) : StoreDetailsState
}

@Immutable
internal sealed interface StoreDetailsIntent : MVIIntent {

    data class SendCommandClicked(val event: StoreCommand) : StoreDetailsIntent
    data class EventClicked(val entry: ServerEventEntry) : StoreDetailsIntent

    data object CloseFocusedEventClicked : StoreDetailsIntent
    data object CopyEventClicked : StoreDetailsIntent
}

@Immutable
internal sealed interface StoreDetailsAction : MVIAction {
    data class CopyToClipboard(val text: String) : StoreDetailsAction
}
