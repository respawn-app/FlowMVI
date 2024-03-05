package pro.respawn.flowmvi.debugger.server

import androidx.compose.runtime.Immutable
import com.benasher44.uuid.Uuid
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.debugger.model.ClientEvent
import pro.respawn.flowmvi.debugger.model.ServerEvent

@Immutable
internal data class ServerClientState(
    val id: Uuid,
    val name: String,
    val isConnected: Boolean,
    val lastConnected: Instant = Clock.System.now(),
)

@Immutable
internal data class ServerEventEntry(
    val id: Uuid,
    val name: String,
    val event: ClientEvent,
    val timestamp: Instant = Clock.System.now(),
)

internal sealed interface ServerState : MVIState {
    data class Error(val e: Exception, val previous: ServerState) : ServerState
    data object Idle : ServerState
    data class Running(
        val clients: PersistentMap<Uuid, ServerClientState> = persistentMapOf(),
        val eventLog: PersistentList<ServerEventEntry> = persistentListOf(),
    ) : ServerState
}

internal sealed interface ServerIntent : MVIIntent {
    data object RestoreRequested : ServerIntent
    data object StopRequested : ServerIntent
    data object ServerStarted : ServerIntent
    data class EventReceived(val event: ClientEvent, val from: Uuid) : ServerIntent
}

internal sealed interface ServerAction : MVIAction {
    data class SendClientEvent(val client: Uuid, val event: ServerEvent) : ServerAction
}
