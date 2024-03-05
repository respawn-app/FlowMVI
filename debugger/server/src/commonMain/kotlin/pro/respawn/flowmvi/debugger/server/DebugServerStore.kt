package pro.respawn.flowmvi.debugger.server

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.debugger.DebuggerDefaults
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreConnected
import pro.respawn.flowmvi.debugger.model.ClientEvent.StoreDisconnected
import pro.respawn.flowmvi.debugger.server.ServerIntent.EventReceived
import pro.respawn.flowmvi.debugger.server.ServerIntent.RestoreRequested
import pro.respawn.flowmvi.debugger.server.ServerIntent.ServerStarted
import pro.respawn.flowmvi.debugger.server.ServerIntent.StopRequested
import pro.respawn.flowmvi.debugger.server.ServerState.Idle
import pro.respawn.flowmvi.debugger.server.ServerState.Running
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
    name = "DebugServer"
    parallelIntents = true
    coroutineContext = Dispatchers.Default
    actionShareBehavior = ActionShareBehavior.Share(overflow = BufferOverflow.DROP_OLDEST)
    debuggable = true
    onOverflow = BufferOverflow.DROP_OLDEST
    enableLogging()
    recover { e ->
        updateState { State.Error(e, this) }
        null
    }
    reduce { intent ->
        when (intent) {
            is RestoreRequested -> updateState<State.Error, _> { previous }
            is StopRequested -> useState { Idle } // needs to be fast
            is ServerStarted -> useState { Running() }
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

private fun ImmutableList<ServerEventEntry>.putEvent(event: ServerEventEntry) = this
    .takeLast(DebuggerDefaults.ServerHistorySize)
    .plus(event)
    .toPersistentList()
