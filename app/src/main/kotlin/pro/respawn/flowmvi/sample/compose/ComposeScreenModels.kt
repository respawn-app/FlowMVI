package pro.respawn.flowmvi.sample.compose

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Stable
sealed class ComposeState : MVIState {

    object Loading : ComposeState()
    object Empty : ComposeState()
    data class Error(val e: Exception?) : ComposeState()
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
