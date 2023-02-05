package pro.respawn.flowmvi.sample.view

import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState

sealed interface BasicActivityState : MVIState {
    data class DisplayingContent(val counter: Int) : BasicActivityState
}

sealed interface BasicActivityIntent : MVIIntent {
    object ClickedFab : BasicActivityIntent
}

sealed interface BasicActivityAction : MVIAction {
    data class ShowSnackbar(val res: Int) : BasicActivityAction
}
