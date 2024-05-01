package pro.respawn.flowmvi.sample.features.decompose.pages

import androidx.compose.runtime.Immutable
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
internal sealed interface PagesComponentState : MVIState {

    data object Loading : PagesComponentState
    data class Error(val e: Exception?) : PagesComponentState
    data object DisplayingPages : PagesComponentState
}

internal sealed interface PagesIntent : MVIIntent {
    data class SelectedPage(val index: Int) : PagesIntent
}

@Immutable
internal sealed interface PagesAction : MVIAction {

    data class SelectPage(val index: Int) : PagesAction
}
