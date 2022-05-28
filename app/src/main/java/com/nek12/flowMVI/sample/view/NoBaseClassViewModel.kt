package com.nek12.flowMVI.sample.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nek12.flowMVI.MVIStore
import com.nek12.flowMVI.sample.R
import com.nek12.flowMVI.sample.view.BasicActivityAction.ShowSnackbar
import com.nek12.flowMVI.sample.view.BasicActivityIntent.ClickedFab
import com.nek12.flowMVI.sample.view.BasicActivityState.DisplayingContent
import com.nek12.flowMVI.withState
import kotlinx.coroutines.delay

//See also BaseClassViewModel
class NoBaseClassViewModel: ViewModel() { //if you don't want to extend MVIViewModel(), use composition instead

    //implement MVIProvider, or just expose store if you want
    val store = MVIStore<BasicActivityState, BasicActivityIntent, BasicActivityAction>(
        scope = viewModelScope,
        initialState = DisplayingContent(0),
        reduce = ::reduce
    )

    private suspend fun reduce(intent: BasicActivityIntent): BasicActivityState {
        when (intent) {
            is ClickedFab -> {
                store.send(ShowSnackbar(R.string.started_processing))

                // Doing long operations will delay intent processing. However, new intents result in new coroutines being launched
                // This means, if we get another intent while delay() is running, it will be processed independently and will start
                // before this function completes. In other words, our state processing will have diverged into two independent branches
                // Don't worry, however, about race conditions on store.currentState : thread-safety of the value variable is guaranteed by flow api
                delay(1000)

                store.send(ShowSnackbar(R.string.finished_processing))
            }
        }

        return store.withState<DisplayingContent, BasicActivityState> {
            copy(counter = counter + 1)
        }
    }

}
