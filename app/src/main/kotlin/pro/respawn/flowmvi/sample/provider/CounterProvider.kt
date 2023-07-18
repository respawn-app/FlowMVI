package pro.respawn.flowmvi.sample.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed
import pro.respawn.flowmvi.sample.R
import pro.respawn.flowmvi.sample.di.ProviderClass
import pro.respawn.flowmvi.sample.repo.CounterRepo

private typealias Ctx = PipelineContext<CounterState, CounterIntent, CounterAction>

class CounterProvider(
    private val param: String,
    private val repo: CounterRepo,
) {

    val store = store<CounterState, CounterIntent, CounterAction>(name, CounterState.Loading) {
        whileSubscribed {
            repo.getTimer()
                .onEach { produceState(it) } // set mapped states
                .flowOn(Dispatchers.Default) // create states out of the main thread
                .collect()
        }
        reduce {
            when (it) {
                is CounterIntent.ClickedCounter -> {
                    action(CounterAction.ShowSnackbar(R.string.started_processing))

                    // Doing long operations will delay intent processing. New intents will NOT result in new coroutines being launched
                    // This means, if we get another intent while delay() is running, it will be processed independently and will start
                    // after this invocation completes.
                    // to solve this, use launch() (example in BaseClassViewModel.kt)
                    delay(1000)

                    action(CounterAction.ShowSnackbar(R.string.finished_processing))
                    updateState<CounterState.DisplayingCounter, _> {
                        copy(counter = counter + 1)
                    }
                }
            }
        }
    }

    private fun Ctx.produceState(timer: Int) = launch {
        updateState {
            // remember that you have to merge states when you are running produceState
            val current = this as? CounterState.DisplayingCounter
            CounterState.DisplayingCounter(timer, current?.counter ?: 0, param)
        }
    }

    companion object : ProviderClass()
}
