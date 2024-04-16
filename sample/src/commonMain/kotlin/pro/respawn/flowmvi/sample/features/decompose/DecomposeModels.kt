package pro.respawn.flowmvi.sample.features.decompose

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
internal sealed interface PagesComponentState : MVIState {

    data object Loading : PagesComponentState
    data class Error(val e: Exception?) : PagesComponentState
    data object DisplayingPages : PagesComponentState
}

@Immutable
data class PageState(
    val index: Int,
    val counter: Int = 0,
) : MVIState

@Immutable
internal sealed interface PageIntent : MVIIntent {

    data object ClickedIncrementCounter : PageIntent
}

@Serializable
data class PageConfig(val page: Int)
