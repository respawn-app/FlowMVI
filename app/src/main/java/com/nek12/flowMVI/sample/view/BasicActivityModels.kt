package com.nek12.flowMVI.sample.view

import com.nek12.flowMVI.MVIAction
import com.nek12.flowMVI.MVIIntent
import com.nek12.flowMVI.MVIState


sealed class BasicActivityState : MVIState {
    data class DisplayingContent(val counter: Int) : BasicActivityState()
}

sealed class BasicActivityIntent : MVIIntent {
    object ClickedFab : BasicActivityIntent()

}

sealed class BasicActivityAction : MVIAction {
    data class ShowSnackbar(val res: Int) : BasicActivityAction()
}
