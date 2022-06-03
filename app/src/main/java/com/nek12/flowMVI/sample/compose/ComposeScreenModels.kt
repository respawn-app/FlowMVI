package com.nek12.flowMVI.sample.compose

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import com.nek12.flowMVI.MVIAction
import com.nek12.flowMVI.MVIIntent
import com.nek12.flowMVI.MVIState

@Stable
sealed class ComposeState: MVIState {

    object Loading: ComposeState()
    object Empty: ComposeState()
    data class DisplayingContent(
        val counter: Int,
        val timer: Int,
    ): ComposeState()
}

@Stable
sealed class ComposeIntent: MVIIntent {

    object ClickedCounter: ComposeIntent()
    object ClickedToBasicActivity: ComposeIntent()
}

@Stable
sealed class ComposeAction: MVIAction {

    object GoToBasicActivity: ComposeAction()
    data class ShowSnackbar(@StringRes val res: Int): ComposeAction()
}
