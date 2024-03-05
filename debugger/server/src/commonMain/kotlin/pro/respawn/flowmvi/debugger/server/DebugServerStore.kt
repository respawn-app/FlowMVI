package pro.respawn.flowmvi.debugger.server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.debugger.server.ServerIntent
import pro.respawn.flowmvi.debugger.server.ServerIntent.EventReceived
import pro.respawn.flowmvi.debugger.server.ServerIntent.RestoreRequested
import pro.respawn.flowmvi.debugger.server.ServerIntent.ServerStarted
import pro.respawn.flowmvi.debugger.server.ServerIntent.StopRequested
import pro.respawn.flowmvi.debugger.server.ServerIntent.StoreConnected
import pro.respawn.flowmvi.debugger.server.ServerIntent.StoreDisconnected
import pro.respawn.flowmvi.debugger.server.ServerState
import pro.respawn.flowmvi.debugger.server.ServerState.Idle
import pro.respawn.flowmvi.debugger.server.ServerState.Running
import pro.respawn.flowmvi.dsl.lazyStore
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.logging.PlatformStoreLogger
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
            is RestoreRequested -> updateState<ServerState.Error, _> { previous }
            is StopRequested -> useState { Idle } // needs to be fast
            is ServerStarted -> useState { Running() }
            is EventReceived -> state {
                copy(
                    eventLog = eventLog.add(
                        ServerEventEntry(
                            intent.event,
                            client = clients[intent.from]?.client ?: return@state this,
                        )
                    )
                )
            }
            is StoreConnected -> state {
                val store = intent.descriptor
                copy(
                    clients = clients.put(
                        key = store.storeId,
                        value = ServerClientState(
                            client = ServerClient(store.storeName, store.storeId),
                            isConnected = true,
                        )
                    )
                )
            }
            is StoreDisconnected -> state {
                val client = clients[intent.id] ?: return@state this
                copy(clients = clients.put(intent.id, client.copy(isConnected = false)))
            }
        }
    }
}

private suspend inline fun Ctx.state(
    crossinline update: suspend Running.() -> State
) = updateState<Running, _>(update)
