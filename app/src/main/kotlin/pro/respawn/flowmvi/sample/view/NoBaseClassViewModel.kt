package pro.respawn.flowmvi.sample.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import pro.respawn.flowmvi.ActionShareBehavior
import pro.respawn.flowmvi.ReducerScope
import pro.respawn.flowmvi.launchedStore
import pro.respawn.flowmvi.sample.R
import pro.respawn.flowmvi.sample.view.BasicActivityAction.ShowSnackbar
import pro.respawn.flowmvi.sample.view.BasicActivityIntent.ClickedFab
import pro.respawn.flowmvi.sample.view.BasicActivityState.DisplayingContent
import pro.respawn.flowmvi.updateState
import pro.respawn.flowmvi.sample.view.BasicActivityAction as Action
import pro.respawn.flowmvi.sample.view.BasicActivityIntent as Intent
import pro.respawn.flowmvi.sample.view.BasicActivityState as State

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
