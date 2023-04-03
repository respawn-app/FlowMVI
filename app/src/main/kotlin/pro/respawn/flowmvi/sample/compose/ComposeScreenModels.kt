package pro.respawn.flowmvi.sample.compose

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState

@Stable
sealed class ComposeState : MVIState {

    object Loading : ComposeState()
    object Empty : ComposeState()
    data class DisplayingContent(
        val timer: Int,
        val counter: Int,
    ) : ComposeState()
}

@Stable
sealed class ComposeIntent : MVIIntent {

    object ClickedCounter : ComposeIntent()
    object ClickedToBasicActivity : ComposeIntent()
}

@Stable
sealed class ComposeAction : MVIAction {

    object GoToBasicActivity : ComposeAction()
    data class ShowSnackbar(@StringRes val res: Int) : ComposeAction()
}
