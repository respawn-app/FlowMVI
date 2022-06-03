package com.nek12.flowMVI.sample.compose

import com.nek12.flowMVI.android.MVIViewModel
import com.nek12.flowMVI.currentState
import com.nek12.flowMVI.sample.R
import com.nek12.flowMVI.sample.compose.ComposeAction.GoToBasicActivity
import com.nek12.flowMVI.sample.compose.ComposeAction.ShowSnackbar
import com.nek12.flowMVI.sample.compose.ComposeIntent.ClickedCounter
import com.nek12.flowMVI.sample.compose.ComposeIntent.ClickedToBasicActivity
import com.nek12.flowMVI.sample.compose.ComposeState.DisplayingContent
import com.nek12.flowMVI.sample.compose.ComposeState.Empty
import com.nek12.flowMVI.sample.compose.ComposeState.Loading
import com.nek12.flowMVI.sample.repo.CounterRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlin.random.Random

/**
 * See also NoBaseClassViewModel
 */
class BaseClassViewModel(
    repo: CounterRepo,
): MVIViewModel<ComposeState, ComposeIntent, ComposeAction>(
    initialState = Loading
) {

    init {
        //Usually this is the place to launch any background processing that is needed
        // to go from initialState to the one you want
        // You can also subscribe to any flows you want here, since the only source of truth in your viewmodel
        // Must now be actions / states flows
        // Use operators onEach/map+setEach to emit states / actions on flow emissions
        incrementCounter(-1)

        repo.getCounter()
            .recover() //recover from exceptions
            .onEmpty(Empty) //set a new state if the flow is empty
            .map(::timerToState) //map values to states
            .setEach() //set mapped states
            .consume() //launch in view model scope
    }

    //Will be when reduce() throws an exception
    override fun recover(from: Exception): ComposeState {
        send(ShowSnackbar(R.string.error))
        return DisplayingContent(0, (currentState as? DisplayingContent)?.timer ?: 0)
    }

    //Will be called each time a subscriber sends a new Intent in a separate coroutine
    override suspend fun reduce(intent: ComposeIntent): ComposeState = when (intent) {

        // Sometimes you can and want to handle certain intents when the view is in a particular state
        // For example, not all buttons may be visible at all times
        // For this, you only handle this intent in the state declared as type parameter of withState,
        // otherwise the function just returns currentState
        is ClickedCounter -> withState<DisplayingContent> {  //this -> DisplayingContent

            //Launch a new coroutine that will set the state later
            incrementCounter(current = counter)

            //Immediately return Loading state
            Loading //^withState
        }

        is ClickedToBasicActivity -> {
            //Send a side-effect to the view
            send(GoToBasicActivity)

            //do not change the state
            currentState
        }
    }

    private fun timerToState(value: Int) = withState<DisplayingContent> {
        copy(timer = value)
    }

    private fun incrementCounter(current: Int) = launchForState {
        delay(1000L)

        if (Random.nextBoolean()) {
            //will be propagated to the recover() handler above, or you can supply your own
            throw IllegalArgumentException("Something bad happened during intent processing")
        }

        //sets this new state after calculations done
        DisplayingContent(current + 1, (currentState as? DisplayingContent)?.timer ?: 0)
    }

}
