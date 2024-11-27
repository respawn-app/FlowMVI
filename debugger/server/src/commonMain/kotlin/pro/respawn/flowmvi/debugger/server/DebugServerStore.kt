package pro.respawn.flowmvi.debugger.server

import com.benasher44.uuid.Uuid
import kotlinx.collections.immutable.ImmutableList
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
import pro.respawn.flowmvi.debugger.server.ServerIntent.RestoreRequested
import pro.respawn.flowmvi.debugger.server.ServerIntent.SendCommand
import pro.respawn.flowmvi.debugger.server.ServerIntent.ServerStarted
import pro.respawn.flowmvi.debugger.server.ServerIntent.StopRequested
import pro.respawn.flowmvi.debugger.server.ServerState.Idle
import pro.respawn.flowmvi.debugger.server.ServerState.Running
import pro.respawn.flowmvi.debugger.server.arch.configuration.debuggable
import pro.respawn.flowmvi.dsl.lazyStore
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.plugins.enableLogging
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
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
            is EventReceived -> state {
                when (val event = intent.event) {
                    is StoreDisconnected -> {
                        val existing = clients[intent.from] ?: return@state this
                        copy(
                            clients = clients.put(intent.from, existing.copy(isConnected = false)),
                            eventLog = eventLog.putEvent(ServerEventEntry(intent.from, existing.name, event)),
                        )
                    }
                    is StoreConnected -> copy(
                        clients = clients.put(intent.from, ServerClientState(intent.from, event.name, true)),
                        eventLog = eventLog.putEvent(ServerEventEntry(intent.from, event.name, event)),
                    )
                    else -> copy(
                        eventLog = eventLog.putEvent(
                            ServerEventEntry(intent.from, clients[intent.from]?.name ?: return@state this, event)
                        )
                    )
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
