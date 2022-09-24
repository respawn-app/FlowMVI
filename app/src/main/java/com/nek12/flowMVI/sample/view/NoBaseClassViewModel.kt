package com.nek12.flowMVI.sample.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nek12.flowMVI.ActionShareBehavior.SHARE
import com.nek12.flowMVI.MVIStore
import com.nek12.flowMVI.sample.R
import com.nek12.flowMVI.sample.view.BasicActivityAction.ShowSnackbar
import com.nek12.flowMVI.sample.view.BasicActivityIntent.ClickedFab
import com.nek12.flowMVI.sample.view.BasicActivityState.DisplayingContent
import com.nek12.flowMVI.withState
import kotlinx.coroutines.delay
import com.nek12.flowMVI.sample.view.BasicActivityAction as Action
import com.nek12.flowMVI.sample.view.BasicActivityIntent as Intent
import com.nek12.flowMVI.sample.view.BasicActivityState as State

// See also BaseClassViewModel
class NoBaseClassViewModel : ViewModel() { // if you don't want to extend MVIViewModel(), use composition instead

    // implement MVIProvider, or just expose store if you want
    val store = MVIStore<State, Intent, Action>(
        initialState = DisplayingContent(0),
        reduce = { reduce(it) },
        behavior = SHARE
    )

    init {
        // Don't forget to launch store intent processing
        store.launch(viewModelScope)
    }

    private suspend fun reduce(intent: Intent): State = with(store) {
        when (intent) {
            is ClickedFab -> {
                send(ShowSnackbar(R.string.started_processing))

                // Doing long operations will delay intent processing. New intents will NOT result in new coroutines being launched
                // This means, if we get another intent while delay() is running, it will be processed independently and will start
                // after this invocation completes.
                delay(1000)

                send(ShowSnackbar(R.string.finished_processing))
            }
        }

        return withState<DisplayingContent, State> {
            copy(counter = counter + 1)
        }
    }
}
