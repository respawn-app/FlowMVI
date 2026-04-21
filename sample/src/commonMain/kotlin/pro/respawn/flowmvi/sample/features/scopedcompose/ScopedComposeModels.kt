package pro.respawn.flowmvi.sample.features.scopedcompose

import androidx.compose.runtime.Immutable
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

// Child list state (shared by feed and notification child stores)
@Immutable
internal sealed interface ListState : MVIState {

    data object Loading : ListState
    data class Loaded(val items: List<String>) : ListState
    data class Error(val message: String) : ListState
}

// Child intents
@Immutable
internal sealed interface ListIntent : MVIIntent {

    data object Refresh : ListIntent
}

// Child actions
@Immutable
internal sealed interface ListAction : MVIAction

// Parent state
@Immutable
internal sealed interface ScopedComposeState : MVIState {

    data object Loading : ScopedComposeState

    data class Content(
        val feed: ListState = ListState.Loading,
        val notifications: ListState = ListState.Loading,
    ) : ScopedComposeState

    data class Error(val message: String) : ScopedComposeState
}

@Immutable
internal sealed interface ScopedComposeIntent : MVIIntent {

    data object ClickedRetry : ScopedComposeIntent
    data object ClickedRefreshFeed : ScopedComposeIntent
    data object ClickedRefreshNotifications : ScopedComposeIntent
    data object ClickedRefreshAll : ScopedComposeIntent

    // Internal intent for initial load completion
    data object DataReady : ScopedComposeIntent
    data class LoadFailed(val message: String) : ScopedComposeIntent
}

@Immutable
internal sealed interface ScopedComposeAction : MVIAction {

    data class ShowError(val message: String) : ScopedComposeAction
}
