package pro.respawn.flowmvi.debugger.server

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.debugger.DebuggerDefaults.ServerHistorySize
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreConnected
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreDisconnected
import pro.respawn.flowmvi.debugger.model.ServerEvent
import pro.respawn.flowmvi.debugger.server.ServerAction.SendClientEvent
import pro.respawn.flowmvi.debugger.server.ServerIntent.EventReceived
import pro.respawn.flowmvi.debugger.server.ServerIntent.MetricsReceived
import pro.respawn.flowmvi.debugger.server.ServerIntent.RestoreRequested
import pro.respawn.flowmvi.debugger.server.ServerIntent.SendCommand
import pro.respawn.flowmvi.debugger.server.ServerIntent.ServerStarted
import pro.respawn.flowmvi.debugger.server.ServerIntent.StopRequested
import pro.respawn.flowmvi.debugger.server.ServerState.Idle
import pro.respawn.flowmvi.debugger.server.ServerState.Running
import pro.respawn.flowmvi.debugger.server.arch.configuration.debuggable
import pro.respawn.flowmvi.debugger.server.util.orEmpty
import pro.respawn.flowmvi.dsl.lazyStore
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.dsl.updateStateImmediate
import pro.respawn.flowmvi.logging.logw
import pro.respawn.flowmvi.metrics.api.MetricsSnapshot
import pro.respawn.flowmvi.plugins.enableLogging
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import kotlin.time.Clock
import kotlin.uuid.Uuid
import pro.respawn.flowmvi.debugger.server.ServerAction as Action
import pro.respawn.flowmvi.debugger.server.ServerIntent as Intent
import pro.respawn.flowmvi.debugger.server.ServerState as State

private typealias Ctx = PipelineContext<State, Intent, Action>

internal fun debugServerStore() = lazyStore<State, Intent, Action>(Idle) {
    configure {
        name = "DebugServer"
        parallelIntents = true
        coroutineContext = Dispatchers.Default
        actionShareBehavior = ActionShareBehavior.Share(overflow = BufferOverflow.DROP_OLDEST)
        debuggable = BuildFlags.debuggable
        allowIdleSubscriptions = true
        onOverflow = BufferOverflow.DROP_OLDEST
    }
    enableLogging()
    recover { e ->
        updateState { State.Error(e, this) }
        null
    }
    reduce { intent ->
        when (intent) {
            is RestoreRequested -> updateState<State.Error, _> { previous }
            is StopRequested -> updateStateImmediate { Idle } // needs to be fast
            is ServerStarted -> updateStateImmediate { Running() }
            is SendCommand -> action(SendClientEvent(intent.storeId, intent.command.event(intent.storeId)))
            is MetricsReceived -> state {
                val key = intent.snapshot.key(intent.from)
                val existing = clients[key]
                if (existing == null) logw {
                    "Received metrics for missing/evicted client $key (id=${intent.from}), creating placeholder entry."
                }
                val client = existing ?: Client(
                    id = intent.from,
                    name = intent.snapshot.meta.storeName,
                    isConnected = false,
                )
                client.copy(
                    name = client.name ?: intent.snapshot.meta.storeName,
                    metrics = client.metrics
                        .add(intent.snapshot)
                        .let { list ->
                            if (list.size <= ServerHistorySize) list
                            else list.takeLast(ServerHistorySize).toPersistentList()
                        }
                )
                    .let { clients.put(key, it) }
                    .let { copy(clients = it) }
            }
            is EventReceived -> state {
                val existing = clients[intent.key]
                when (val event = intent.event) {
                    is StoreDisconnected -> {
                        existing ?: return@state this
                        copy(
                            clients = clients.put(
                                key = intent.key,
                                value = existing.copy(
                                    isConnected = false,
                                    events = existing.events.putEvent(ServerEventEntry(event))
                                ),
                            ),
                        )
                    }
                    is StoreConnected -> copy(
                        clients = clients.put(
                            key = intent.key,
                            value = (existing ?: Client(id = intent.from, name = event.name)).copy(
                                id = intent.from,
                                name = event.name,
                                isConnected = true,
                                lastConnected = Clock.System.now(),
                                events = existing?.events.orEmpty().putEvent(ServerEventEntry(event)),
                            ),
                        ),
                    )
                    else -> {
                        if (existing == null) logw {
                            "It should not be possible to send events to non-connected clients, event: $intent"
                        }
                        val client = existing ?: return@state copy(
                            clients = clients.put(
                                key = intent.key,
                                value = Client(
                                    id = intent.from,
                                    name = intent.event.storeName,
                                    events = persistentListOf(ServerEventEntry(event))
                                )
                            )
                        )
                        copy(
                            clients = clients.put(
                                key = intent.key,
                                value = client.copy(
                                    events = client.events.putEvent(ServerEventEntry(event))
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}

private suspend inline fun Ctx.state(
    crossinline update: suspend Running.() -> State
) = updateState<Running, _>(update)

private fun ImmutableList<ServerEventEntry>.putEvent(event: ServerEventEntry) = sequenceOf(event)
    .plus(this)
    .take(ServerHistorySize)
    .toPersistentList()

private fun StoreCommand.event(storeId: Uuid) = when (this) {
    StoreCommand.Stop -> ServerEvent.Stop(storeId)
    StoreCommand.ResendIntent -> ServerEvent.ResendLastIntent(storeId)
    StoreCommand.RollbackState -> ServerEvent.RollbackState(storeId)
    StoreCommand.ResendAction -> ServerEvent.ResendLastAction(storeId)
    StoreCommand.RethrowException -> ServerEvent.RethrowLastException(storeId)
    StoreCommand.SetInitialState -> ServerEvent.RollbackToInitialState(storeId)
}

internal val EventReceived.key get() = StoreKey(event.storeName, from)
internal fun MetricsSnapshot.key(id: Uuid) = StoreKey(meta.storeName, meta.storeId ?: id)
