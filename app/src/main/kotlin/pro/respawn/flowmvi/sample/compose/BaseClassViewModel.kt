package pro.respawn.flowmvi.sample.compose

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import pro.respawn.flowmvi.android.MVIViewModel
import pro.respawn.flowmvi.sample.R
import pro.respawn.flowmvi.sample.compose.ComposeAction.GoToBasicActivity
import pro.respawn.flowmvi.sample.compose.ComposeAction.ShowSnackbar
import pro.respawn.flowmvi.sample.compose.ComposeIntent.ClickedCounter
import pro.respawn.flowmvi.sample.compose.ComposeIntent.ClickedToBasicActivity
import pro.respawn.flowmvi.sample.compose.ComposeState.DisplayingContent
import pro.respawn.flowmvi.sample.compose.ComposeState.Loading
import pro.respawn.flowmvi.sample.repo.CounterRepo
import kotlin.random.Random

/**
 * See also NoBaseClassViewModel
 */
class BaseClassViewModel(
    repo: CounterRepo,
) : MVIViewModel<ComposeState, ComposeIntent, ComposeAction>(initial = Loading) {

    init {
        // Usually this is the place to launch any background processing that is needed
        // to go from initialState to the one you want
        // You can also subscribe to any flows you want here, since the only source of truth in your viewmodel
        // Must now be actions / states flows
        // Use operators onEach/map+setEach to emit states / actions on flow emissions
        repo.getTimer()
            .onEach(::produceState) // set mapped states
            .recover() // recover from exceptions
            .flowOn(Dispatchers.Default) // create states out of the main thread
            .consume() // launch in view model scope
    }

    // Will be called when reducer or any child coroutine throws an exception
    // usually we would display a full-screen error here
    override suspend fun recover(e: Exception): ComposeState = ComposeState.Error(e)

    // Will be called each time a subscriber sends a new Intent in a separate coroutine
    override suspend fun reduce(intent: ComposeIntent) {
        when (intent) {
            // Sometimes you can and want to handle certain intents when the view is in a particular state
            // For example, not all buttons may be visible at all times
            // For this, you only handle this intent in the state declared as type parameter of withState,
            // otherwise the function just returns currentState
            is ClickedCounter -> updateState<DisplayingContent> { // this -> DisplayingContent

                // Launch a new coroutine that will set the state later
                incrementCounter()

                // Immediately return Loading state
                Loading // ^withState
            }

            is ClickedToBasicActivity -> send(GoToBasicActivity)
        }
    }

    private suspend fun produceState(timer: Int) = updateState {
        val current = this as? DisplayingContent
        DisplayingContent(
            timer = timer,
            counter = current?.counter ?: 0,
        )
    }

    private fun DisplayingContent.incrementCounter() = launchRecovering {
        // sets this new state after calculations done
        updateState {
            delay(1000L)
            require(Random.nextBoolean()) { "Something bad happened during intent processing" }
            copy(counter = counter + 1)
        }
    }
}
