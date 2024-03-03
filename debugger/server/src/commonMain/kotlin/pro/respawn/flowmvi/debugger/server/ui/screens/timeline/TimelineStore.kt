package pro.respawn.flowmvi.debugger.server.ui.screens.timeline

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
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.CloseFocusedEntryClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.EntryClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.EventFilterSelected
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.RestartClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.RetryClicked
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.StoreFilterSelected
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineState
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineState.DisplayingTimeline
import pro.respawn.flowmvi.debugger.server.ui.type
import pro.respawn.flowmvi.dsl.collect
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.plugins.platformLoggingPlugin
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed
import pro.respawn.flowmvi.util.typed
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineAction as Action
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent as Intent
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineState as State

internal fun timelineStore(scope: CoroutineScope) = store<State, Intent, Action>(State.Loading, scope) {
    val timezone = TimeZone.currentSystemDefault()
    val filters = MutableStateFlow(TimelineFilters())

    name = "TimelineStore"
    install(platformLoggingPlugin())
    parallelIntents = true
    recover {
        updateState { TimelineState.Error(it) }
        null
    }
    whileSubscribed {
        DebugServer.store.collect {
            combine(
                states,
                filters
            ) { state, currentFilters ->
                updateState {
                    when (state) {
                        is ServerState.Error -> TimelineState.Error(state.e)
                        is ServerState.Running -> DisplayingTimeline(
                            focusedEvent = typed<DisplayingTimeline>()?.focusedEvent,
                            filters = currentFilters,
                            currentEvents = state.eventLog,
                            stores = state.clients
                                .map { StoreItem(it.key, it.value.client.name) }
                                .toPersistentList(),
                        )
                    }
                }
            }.consume(Dispatchers.Default)
        }
    }
    reduce { intent ->
        when (intent) {
            is RestartClicked -> DebugServer.relaunch()
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
                        storeName = intent.entry.client.name,
                        type = intent.entry.event.type,
                        event = intent.entry.event,
                    )
                )
            }
        }
    }
}
