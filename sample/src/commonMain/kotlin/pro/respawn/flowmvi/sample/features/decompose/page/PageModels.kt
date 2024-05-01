package pro.respawn.flowmvi.sample.features.decompose.page

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
@Serializable
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
