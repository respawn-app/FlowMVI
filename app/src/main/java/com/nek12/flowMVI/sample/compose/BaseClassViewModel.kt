package com.nek12.flowMVI.sample.compose

import com.nek12.flowMVI.android.MVIViewModel
import com.nek12.flowMVI.sample.R
import com.nek12.flowMVI.sample.compose.ComposeAction.GoToBasicActivity
import com.nek12.flowMVI.sample.compose.ComposeAction.ShowSnackbar
import com.nek12.flowMVI.sample.compose.ComposeIntent.ClickedCounter
import com.nek12.flowMVI.sample.compose.ComposeIntent.ClickedToBasicActivity
import com.nek12.flowMVI.sample.compose.ComposeState.DisplayingContent
import com.nek12.flowMVI.sample.compose.ComposeState.Loading
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * See also NoBaseClassViewModel
 */
class BaseClassViewModel: MVIViewModel<ComposeState, ComposeIntent, ComposeAction>() {

    override val initialState get() = Loading

    init {
        //Usually this is the place to launch any background processing that is needed
        // to go from initialState to the one you want
        // You can also subscribe to any flows you want here, since the only source of truth in your viewmodel
        // Must now be actions / states flows
        // Use operators onEach() and launchIn() to emit states / actions on flow emissions
        incrementCounter(-1)
    }

    //Will be when reduce() throws an exception
    override fun recover(from: Exception): ComposeState {
        send(ShowSnackbar(R.string.error))
        return DisplayingContent(0)
    }

    //Will be called each time a subscriber sends a new Intent in a separate coroutine
    override suspend fun reduce(intent: ComposeIntent): ComposeState = when (intent) {

        // Sometimes you can and want to handle certain intents when the view is in a particular state
        // For example, not all buttons may be visible at all times
        // For this, you only handle this intent in the state declared as type parameter of withState,
        // otherwise the function just returns currentState
        ClickedCounter -> withState<DisplayingContent> { //this -> DisplayingContent

            //Launch a new coroutine that will set the state later
            incrementCounter(current = this.counter)

            //Immediately return Loading state
            Loading //^withState
        }

        ClickedToBasicActivity -> {
            //Send a side-effect to the view
            send(GoToBasicActivity)

            //do not change the state
            currentState
        }
    }

    private fun incrementCounter(current: Int) = launchForState {
        delay(1000L) //simulate long processing

        if (Random.nextBoolean()) {
            //will be propagated to the recover() handler above, or you can supply your own
            throw IllegalArgumentException("Something bad happened during intent processing")
        }

        //sets this new state after calculations done
        DisplayingContent(current + 1)
    }

}
