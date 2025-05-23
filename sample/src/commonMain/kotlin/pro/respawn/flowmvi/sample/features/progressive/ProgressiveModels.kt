package pro.respawn.flowmvi.sample.features.progressive

import androidx.compose.runtime.Immutable
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
data class Item(val index: Int, val title: String)

// region feed
@Immutable
internal sealed interface FeedState : MVIState {

    data object Loading : FeedState
    data class Content(val items: List<Item>) : FeedState
}

@Immutable
internal sealed interface FeedAction : ProgressiveAction {

    data object ShowLoadedMessage : FeedAction
}

// endregion

// region suggestions

@Immutable
internal sealed interface SuggestionsState : MVIState {

    data object Loading : SuggestionsState
    data class Content(val items: List<Item>) : SuggestionsState
}

// endregion

@Immutable
internal data class ProgressiveState(
    val feed: FeedState = FeedState.Loading,
    val suggestions: SuggestionsState = SuggestionsState.Loading,
) : MVIState

@Immutable
internal sealed interface ProgressiveIntent : MVIIntent {

}

@Immutable
internal sealed interface ProgressiveAction : MVIAction {

}
