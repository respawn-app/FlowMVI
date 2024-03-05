package pro.respawn.flowmvi.debugger.server.ui.screens.timeline

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import pro.respawn.flowmvi.debugger.server.DebugServer
import pro.respawn.flowmvi.debugger.server.ServerIntent.RestoreRequested
import pro.respawn.flowmvi.debugger.server.ServerState
import pro.respawn.flowmvi.debugger.server.ui.HostForm
import pro.respawn.flowmvi.debugger.server.ui.PortForm
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineAction.ScrollToItem
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.AutoScrollToggled
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.CloseFocusedEntryClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.EntryClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.EventFilterSelected
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.HostChanged
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.PortChanged
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.RetryClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.StoreFilterSelected
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineState.ConfiguringServer
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineState.DisplayingTimeline
import pro.respawn.flowmvi.debugger.server.ui.type
import pro.respawn.flowmvi.dsl.collect
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.plugins.enableLogging
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed
import pro.respawn.flowmvi.util.typed
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineAction as Action
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent as Intent
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineState as State

@Suppress("UnnecessaryParentheses")
internal fun timelineStore(scope: CoroutineScope) = store<State, Intent, Action>(ConfiguringServer(), scope) {
    val timezone = TimeZone.currentSystemDefault()
    val filters = MutableStateFlow(TimelineFilters())
    name = "TimelineStore"
    parallelIntents = true
    debuggable = true
    enableLogging()
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
                        is ServerState.Idle -> typed<ConfiguringServer>() ?: ConfiguringServer()
                        is ServerState.Error -> State.Error(state.e)
                        is ServerState.Running -> DisplayingTimeline(
                            autoScroll = current?.autoScroll ?: true,
                            focusedEvent = current?.focusedEvent,
                            filters = currentFilters,
                            currentEvents = state.eventLog
                                .asSequence()
                                .run {
                                    if (current == null) return@run this
                                    val id = current.filters.store?.id
                                    filter { it.event.type in current.filters.events && (id == null || id == it.id) }
                                }
                                .toPersistentList(),
                            stores = state.clients
                                .asSequence()
                                .map { StoreItem(it.key, it.value.name) }
                                .toPersistentList(),
                        ).also {
                            if (current == null || !it.autoScroll) return@also
                            if (current.currentEvents.size >= it.currentEvents.size) return@also
                            action(ScrollToItem(it.currentEvents.lastIndex))
                        }
                    }
                }
            }.consume(Dispatchers.Default)
        }
    }
    reduce { intent ->
        when (intent) {
            is HostChanged -> updateState<ConfiguringServer, _> { copy(host = HostForm(intent.host)) }
            is PortChanged -> updateState<ConfiguringServer, _> { copy(port = PortForm(intent.port)) }
            is AutoScrollToggled -> updateState<DisplayingTimeline, _> { copy(autoScroll = !autoScroll) }
            is Intent.StartServerClicked -> updateState<ConfiguringServer, _> {
                if (canStart) {
                    DebugServer.start(host = host.value, port = port.value.toInt())
                    DisplayingTimeline(persistentListOf(), persistentListOf())
                } else this
            }
            is Intent.StopServerClicked -> updateState<DisplayingTimeline, _> {
                DebugServer.stop()
                ConfiguringServer()
            }
            is RetryClicked -> DebugServer.store.intent(RestoreRequested)
            is StoreFilterSelected -> filters.update { it.copy(store = intent.store) }
            is EventFilterSelected -> filters.update {
                it.copy(
                    events = it.events.toPersistentSet().run {
                        if (intent.filter in this) remove(intent.filter) else add(intent.filter)
                    }
                )
            }
            is CloseFocusedEntryClicked -> updateState<DisplayingTimeline, _> { copy(focusedEvent = null) }
            is EntryClicked -> updateState<DisplayingTimeline, _> {
                copy(
                    focusedEvent = FocusedEvent(
                        timestamp = intent.entry.timestamp.toLocalDateTime(timezone),
                        storeName = intent.entry.name,
                        type = intent.entry.event.type,
                        event = intent.entry.event,
                    )
                )
            }
        }
    }
}
