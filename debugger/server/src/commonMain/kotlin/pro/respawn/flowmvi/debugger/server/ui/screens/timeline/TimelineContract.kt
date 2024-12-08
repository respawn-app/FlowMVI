package pro.respawn.flowmvi.debugger.server.ui.screens.timeline

import androidx.compose.runtime.Immutable
import kotlin.uuid.Uuid
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.debugger.model.ClientEvent
import pro.respawn.flowmvi.debugger.server.ServerEventEntry
import pro.respawn.flowmvi.debugger.server.util.type

internal enum class EventType {
    Intent, Action, StateChange, Subscription, Connection, Exception, Initialization
}

@Immutable
internal data class TimelineFilters(
    val events: Set<EventType> = EventType.entries.toSet(),
)

@Immutable
internal data class StoreItem(
    val id: Uuid,
    val name: String,
    val isConnected: Boolean,
)

@Immutable
internal data class FocusedEvent(
    val timestamp: LocalDateTime,
    val storeName: String,
    val type: EventType,
    val event: ClientEvent,
    val id: Uuid,
) {
    constructor(entry: ServerEventEntry) : this(
        timestamp = entry.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()),
        storeName = entry.name,
        type = entry.event.type,
        event = entry.event,
        id = entry.id,
    )
}

@Immutable
internal sealed interface TimelineState : MVIState {

    data object Loading : TimelineState

    data class Error(val e: Exception) : TimelineState

    data class DisplayingTimeline(
        val stores: ImmutableList<StoreItem>,
        val currentEvents: ImmutableList<ServerEventEntry>,
        val focusedEvent: FocusedEvent? = null,
        val filters: TimelineFilters = TimelineFilters(),
        val autoScroll: Boolean = true,
    ) : TimelineState
}

@Immutable
internal sealed interface TimelineIntent : MVIIntent {

    data class EventFilterSelected(val filter: EventType) : TimelineIntent
    data object StopServerClicked : TimelineIntent
    data class StoreSelected(val store: StoreItem) : TimelineIntent
    data object RetryClicked : TimelineIntent
    data class EventClicked(val entry: ServerEventEntry) : TimelineIntent
    data object CopyEventClicked : TimelineIntent
    data object CloseFocusedEventClicked : TimelineIntent
    data object AutoScrollToggled : TimelineIntent
}

@Immutable
internal sealed interface TimelineAction : MVIAction {

    data class CopyToClipboard(val text: String) : TimelineAction
    data class ScrollToItem(val index: Int) : TimelineAction
    data class GoToStoreDetails(val storeId: Uuid) : TimelineAction
    data object GoToConnect : TimelineAction
}
