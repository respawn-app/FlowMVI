package pro.respawn.flowmvi.debugger.server.ui.screens.timeline

import androidx.compose.runtime.Immutable
import com.benasher44.uuid.Uuid
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.datetime.LocalDateTime
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.debugger.DebuggerDefaults
import pro.respawn.flowmvi.debugger.model.ClientEvent
import pro.respawn.flowmvi.debugger.server.ServerEventEntry
import pro.respawn.kmmutils.inputforms.Input
import pro.respawn.kmmutils.inputforms.dsl.input
import pro.respawn.kmmutils.inputforms.dsl.isValid

internal enum class EventType {
    Intent, Action, StateChange, Subscription, Connection, Exception, Initialization
}

@Immutable
internal data class TimelineFilters(
    val store: StoreItem? = null,
    val events: ImmutableSet<EventType> = EventType.entries.toImmutableSet(),
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
)

@Immutable
internal sealed interface TimelineState : MVIState {

    data class Error(val e: Exception) : TimelineState

    data class ConfiguringServer(
        val host: Input = input(DebuggerDefaults.LocalHost),
        val port: Input = input(DebuggerDefaults.Port.toString()),
    ) : TimelineState {
        val canStart = host.isValid && port.isValid
    }
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
    data class PortChanged(val port: String) : TimelineIntent
    data class HostChanged(val host: String) : TimelineIntent
    data object StartServerClicked : TimelineIntent
    data object CopyEventClicked : TimelineIntent
    data object StopServerClicked : TimelineIntent
    data class StoreFilterSelected(val store: StoreItem?) : TimelineIntent
    data object RetryClicked : TimelineIntent
    data class EntryClicked(val entry: ServerEventEntry) : TimelineIntent
    data object CloseFocusedEntryClicked : TimelineIntent
    data object AutoScrollToggled : TimelineIntent
}

@Immutable
internal sealed interface TimelineAction : MVIAction {

    data class CopyToClipboard(val text: String) : TimelineAction
    data class ScrollToItem(val index: Int) : TimelineAction
}
