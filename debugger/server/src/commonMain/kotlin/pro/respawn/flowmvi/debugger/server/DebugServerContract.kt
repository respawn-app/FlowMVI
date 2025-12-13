package pro.respawn.flowmvi.debugger.server

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.debugger.model.ClientEvent
import pro.respawn.flowmvi.debugger.model.ServerEvent
import pro.respawn.flowmvi.metrics.api.MetricsSnapshot
import pro.respawn.kmmutils.common.takeIfValid
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal enum class StoreCommand {
    Stop, ResendIntent, RollbackState, ResendAction, RethrowException, SetInitialState
}

@JvmInline
@Serializable
value class StoreKey(val value: String) {

    constructor(name: String?, id: Uuid) : this(name?.takeIfValid() ?: id.toString())

    override fun toString(): String = value
}

data class SessionKey(
    val id: Uuid,
    val name: String?
) {

    val key = StoreKey(name, id)
}

internal data class Client(
    val id: Uuid,
    val name: String?,
    val events: PersistentList<ServerEventEntry> = persistentListOf(),
    val metrics: PersistentList<MetricsSnapshot> = persistentListOf(),
    val isConnected: Boolean = true,
    val lastConnected: Instant = Clock.System.now(),
) {

    val key get() = StoreKey(name, id)
}

@Immutable
internal data class ServerEventEntry(
    val event: ClientEvent,
    val timestamp: Instant = Clock.System.now(),
    val id: Uuid = Uuid.random(),
)

@Immutable
internal sealed interface ServerState : MVIState {

    data class Error(val e: Exception, val previous: ServerState) : ServerState
    data object Idle : ServerState
    data class Running(
        val clients: PersistentMap<StoreKey, Client> = persistentMapOf(),
    ) : ServerState {

        override fun toString() = "Running(clients=${clients.count { it.value.isConnected }})"
    }
}

@Immutable
internal sealed interface ServerIntent : MVIIntent {

    data object RestoreRequested : ServerIntent
    data object StopRequested : ServerIntent
    data object ServerStarted : ServerIntent
    data class EventReceived(val event: ClientEvent, val from: Uuid) : ServerIntent
    data class SendCommand(val command: StoreCommand, val storeId: Uuid) : ServerIntent
    data class MetricsReceived(val snapshot: MetricsSnapshot, val from: Uuid) : ServerIntent
}

@Immutable
internal sealed interface ServerAction : MVIAction {

    data class SendClientEvent(val client: Uuid, val event: ServerEvent) : ServerAction
}
