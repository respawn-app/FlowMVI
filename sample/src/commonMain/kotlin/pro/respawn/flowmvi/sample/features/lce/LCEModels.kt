package pro.respawn.flowmvi.sample.features.lce

import androidx.compose.runtime.Immutable
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
data class LCEItem(val index: Int)

@Immutable
internal sealed interface LCEState : MVIState {

    data object Loading : LCEState
    data class Error(val e: Exception?) : LCEState
    data class Content(val items: List<LCEItem>) : LCEState
}

@Immutable
internal sealed interface LCEIntent : MVIIntent {

    data object ClickedRefresh : LCEIntent
}
