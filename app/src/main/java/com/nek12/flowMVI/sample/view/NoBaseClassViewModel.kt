package com.nek12.flowMVI.sample.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nek12.flowMVI.ActionShareBehavior
import com.nek12.flowMVI.ReducerScope
import com.nek12.flowMVI.launchedStore
import com.nek12.flowMVI.sample.R
import com.nek12.flowMVI.sample.view.BasicActivityAction.ShowSnackbar
import com.nek12.flowMVI.sample.view.BasicActivityIntent.ClickedFab
import com.nek12.flowMVI.sample.view.BasicActivityState.DisplayingContent
import com.nek12.flowMVI.updateState
import kotlinx.coroutines.delay
import com.nek12.flowMVI.sample.view.BasicActivityAction as Action
import com.nek12.flowMVI.sample.view.BasicActivityIntent as Intent
import com.nek12.flowMVI.sample.view.BasicActivityState as State

// See also BaseClassViewModel
// if you don't want to extend MVIViewModel(), use composition instead
class NoBaseClassViewModel : ViewModel() {

    // implement MVIProvider, or just expose store if you want
    val store by launchedStore<State, Intent, Action>(
        scope = viewModelScope,
        initial = DisplayingContent(0),
        behavior = ActionShareBehavior.Share(),
    ) { reduce(it) }

    private suspend fun ReducerScope<State, Intent, Action>.reduce(intent: Intent) {
        when (intent) {
            is ClickedFab -> {
                ShowSnackbar(R.string.started_processing).send()

                // Doing long operations will delay intent processing. New intents will NOT result in new coroutines being launched
                // This means, if we get another intent while delay() is running, it will be processed independently and will start
                // after this invocation completes.
                // to solve this, use launchRecovering() (example in BaseClassViewModel.kt)
                delay(1000)

                ShowSnackbar(R.string.finished_processing).send()
            }
        }

        updateState<DisplayingContent, _> {
            copy(counter = counter + 1)
        }
    }
}
