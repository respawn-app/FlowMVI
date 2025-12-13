package pro.respawn.flowmvi.debugger.server.ui.screens.storedetails

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.debugger.server.DebugServer
import pro.respawn.flowmvi.debugger.server.ServerIntent
import pro.respawn.flowmvi.debugger.server.ServerState
import pro.respawn.flowmvi.debugger.server.StoreKey
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
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.dsl.withState
import pro.respawn.flowmvi.plugins.delegate.DelegationMode
import pro.respawn.flowmvi.plugins.delegate.delegate
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed
import pro.respawn.flowmvi.util.typed

private typealias Ctx = PipelineContext<StoreDetailsState, StoreDetailsIntent, StoreDetailsAction>

@OptIn(ExperimentalFlowMVIAPI::class)
internal class StoreDetailsContainer(
    private val storeKey: StoreKey,
    configuration: StoreConfiguration,
) : Container<StoreDetailsState, StoreDetailsIntent, StoreDetailsAction> {

    override val store = store(StoreDetailsState.Loading) {
        configure(configuration, "StoreDetailsStore")
        val serverState by delegate(DebugServer.store, DelegationMode.Immediate(), start = false)
        recover {
            updateState { StoreDetailsState.Error(it) }
            null
        }
        whileSubscribed {
            serverState.onEach { state ->
                updateState {
                    val current = typed<DisplayingStore>()
                    when (state) {
                        is ServerState.Error -> StoreDetailsState.Error(state.e)
                        is ServerState.Idle -> StoreDetailsState.Disconnected
                        is ServerState.Running -> state.clients[storeKey]?.run {
                            DisplayingStore(
                                id = id,
                                name = name,
                                connected = isConnected,
                                focusedEvent = current?.focusedEvent,
                                eventLog = events
                            )
                        } ?: return@updateState StoreDetailsState.Disconnected
                    }
                }
            }.consume(Dispatchers.Default)
        }
        reduce { intent ->
            when (intent) {
                is CloseFocusedEventClicked -> updateState<DisplayingStore, _> { copy(focusedEvent = null) }
                is SendCommandClicked -> withState<DisplayingStore, _> {
                    DebugServer.store.intent(ServerIntent.SendCommand(intent.event, id))
                }
                is CopyEventClicked -> withState<DisplayingStore, _> {
                    val event = focusedEvent?.event?.representation ?: return@withState
                    action(CopyToClipboard(event))
                }
                is EventClicked -> updateState<DisplayingStore, _> {
                    if (intent.entry.id == focusedEvent?.id) return@updateState copy(focusedEvent = null)
                    copy(focusedEvent = FocusedEvent(intent.entry, storeKey))
                }
            }
        }
    }
}
