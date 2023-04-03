package pro.respawn.flowmvi.sample.view

import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState

sealed interface BasicState : MVIState {
    object Loading : BasicState
    data class DisplayingCounter(
        val counter: Int,
        val param: String,
    ) : BasicState
}

sealed interface BasicIntent : MVIIntent {
    object ClickedFab : BasicIntent
}

sealed interface BasicAction : MVIAction {
    data class ShowSnackbar(val res: Int) : BasicAction
}
