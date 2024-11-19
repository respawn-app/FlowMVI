package pro.respawn.flowmvi.debugger.server.ui.screens.timeline

import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.debugger.server.DebugServer
import pro.respawn.flowmvi.debugger.server.ServerIntent.RestoreRequested
import pro.respawn.flowmvi.debugger.server.ServerState
import pro.respawn.flowmvi.debugger.server.arch.configuration.StoreConfiguration
import pro.respawn.flowmvi.debugger.server.arch.configuration.configure
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineAction.CopyToClipboard
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineAction.GoToStoreDetails
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineAction.ScrollToItem
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.AutoScrollToggled
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.CloseFocusedEventClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.CopyEventClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.EventClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.EventFilterSelected
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.RetryClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.StopServerClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.StoreSelected
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineState.DisplayingTimeline
import pro.respawn.flowmvi.debugger.server.util.representation
import pro.respawn.flowmvi.debugger.server.util.type
import pro.respawn.flowmvi.dsl.collect
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.dsl.withState
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed
import pro.respawn.flowmvi.util.typed
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent as Intent
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineState as State

private typealias Ctx = PipelineContext<State, Intent, TimelineAction>

internal class TimelineContainer(
    configuration: StoreConfiguration,
) : Container<State, Intent, TimelineAction> {

    private val filters = MutableStateFlow(TimelineFilters())

    override val store = store(State.Loading) {
        configure(configuration, "Timeline")
        recover {
            updateState { State.Error(it) }
            null
        }
        whileSubscribed {
            DebugServer.store.collect {
                combine(
                    states,
                    filters
                ) { state, currentFilters ->
                    updateState {
                        val current = typed<DisplayingTimeline>()
                        when (state) {
                            is ServerState.Idle -> State.Loading
                            is ServerState.Error -> State.Error(state.e)
                            is ServerState.Running -> DisplayingTimeline(
                                autoScroll = current?.autoScroll ?: true,
                                focusedEvent = current?.focusedEvent,
                                filters = currentFilters,
                                currentEvents = state.eventLog
                                    .asSequence()
                                    .run {
                                        if (current == null) return@run this
                                        filter { it.event.type in current.filters.events }
                                    }
                                    .toPersistentList(),
                                stores = state.clients
                                    .asSequence()
                                    .map { StoreItem(it.key, it.value.name, it.value.isConnected) }
                                    .toPersistentList(),
                            ).also {
                                val hasFocusedItem = it.focusedEvent != null
                                val hasEvents = it.currentEvents.isNotEmpty()
                                if (current == null || !it.autoScroll) return@also
                                if (hasFocusedItem || !hasEvents) return@also
                                action(ScrollToItem(it.currentEvents.lastIndex))
                            }
                        }
                    }
                }.consume(Dispatchers.Default)
            }
        }
        reduce { intent ->
            when (intent) {
                is StoreSelected -> action(GoToStoreDetails(intent.store.id))
                is CloseFocusedEventClicked -> updateState<DisplayingTimeline, _> { copy(focusedEvent = null) }
                is RetryClicked -> DebugServer.store.intent(RestoreRequested)
                is CopyEventClicked -> withState<DisplayingTimeline, _> {
                    val event = focusedEvent?.event?.representation ?: return@withState
                    action(CopyToClipboard(event))
                }
                is AutoScrollToggled -> updateState<DisplayingTimeline, _> {
                    copy(autoScroll = !autoScroll)
                }
                is StopServerClicked -> updateState<DisplayingTimeline, _> {
                    launch {
                        DebugServer.stop()
                        action(TimelineAction.GoToConnect)
                    }
                    State.Loading
                }
                is EventFilterSelected -> filters.update {
                    it.copy(
                        events = it.events.toPersistentSet().run {
                            if (intent.filter in this) remove(intent.filter) else add(intent.filter)
                        }
                    )
                }
                is EventClicked -> updateState<DisplayingTimeline, _> {
                    if (intent.entry.event == focusedEvent?.event) return@updateState copy(focusedEvent = null)
                    copy(
                        focusedEvent = FocusedEvent(
                            timestamp = intent.entry.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()),
                            storeName = intent.entry.name,
                            type = intent.entry.event.type,
                            event = intent.entry.event,
                            id = intent.entry.id,
                        )
                    )
                }
            }
        }
    }
}
