package pro.respawn.flowmvi.debugger.server.ui.screens.storedetails

import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.debugger.server.DebugServer
import pro.respawn.flowmvi.debugger.server.ServerIntent
import pro.respawn.flowmvi.debugger.server.ServerState
import pro.respawn.flowmvi.debugger.server.arch.configuration.StoreConfiguration
import pro.respawn.flowmvi.debugger.server.arch.configuration.configure
import pro.respawn.flowmvi.debugger.server.ui.screens.storedetails.StoreDetailsAction.CopyToClipboard
import pro.respawn.flowmvi.debugger.server.ui.screens.storedetails.StoreDetailsIntent.CloseFocusedEventClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.storedetails.StoreDetailsIntent.CopyEventClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.storedetails.StoreDetailsIntent.EventClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.storedetails.StoreDetailsIntent.SendCommandClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.storedetails.StoreDetailsState.DisplayingStore
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.FocusedEvent
import pro.respawn.flowmvi.debugger.server.util.representation
import pro.respawn.flowmvi.dsl.collect
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.dsl.withState
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed
import pro.respawn.flowmvi.util.typed
import kotlin.uuid.Uuid

private typealias Ctx = PipelineContext<StoreDetailsState, StoreDetailsIntent, StoreDetailsAction>

internal class StoreDetailsContainer(
    private val id: Uuid,
    configuration: StoreConfiguration,
) : Container<StoreDetailsState, StoreDetailsIntent, StoreDetailsAction> {

    override val store = store(StoreDetailsState.Loading) {
        configure(configuration, "StoreDetailsStore")
        recover {
            updateState { StoreDetailsState.Error(it) }
            null
        }
        whileSubscribed {
            DebugServer.store.collect {
                states.onEach { state ->
                    updateState {
                        val current = typed<DisplayingStore>()
                        when (state) {
                            is ServerState.Error -> StoreDetailsState.Error(state.e)
                            is ServerState.Idle -> StoreDetailsState.Disconnected
                            is ServerState.Running -> {
                                val client = state.clients[id] ?: return@updateState StoreDetailsState.Disconnected
                                DisplayingStore(
                                    id = id,
                                    name = client.name,
                                    connected = client.isConnected,
                                    focusedEvent = current?.focusedEvent,
                                    eventLog = state.eventLog
                                        .asSequence()
                                        .filter { it.storeId == client.id }
                                        .toPersistentList()
                                )
                            }
                        }
                    }
                }.consume(Dispatchers.Default)
            }
        }
        reduce { intent ->
            when (intent) {
                is CloseFocusedEventClicked -> updateState<DisplayingStore, _> { copy(focusedEvent = null) }
                is SendCommandClicked -> DebugServer.store.intent(ServerIntent.SendCommand(intent.event, id))
                is CopyEventClicked -> withState<DisplayingStore, _> {
                    val event = focusedEvent?.event?.representation ?: return@withState
                    action(CopyToClipboard(event))
                }
                is EventClicked -> updateState<DisplayingStore, _> {
                    if (intent.entry.id == focusedEvent?.id) return@updateState copy(focusedEvent = null)
                    copy(focusedEvent = FocusedEvent(intent.entry))
                }
            }
        }
    }
}
