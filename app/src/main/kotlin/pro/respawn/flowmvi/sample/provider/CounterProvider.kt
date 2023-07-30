package pro.respawn.flowmvi.sample.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.android.plugins.androidLoggingPlugin
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed
import pro.respawn.flowmvi.sample.R
import pro.respawn.flowmvi.sample.repo.CounterRepo
import kotlin.random.Random

private typealias Ctx = PipelineContext<CounterState, CounterIntent, CounterAction>

class CounterProvider(
    private val repo: CounterRepo,
) {

    val store = store<CounterState, CounterIntent, CounterAction> {
        name = "Counter"
        install(androidLoggingPlugin())
        whileSubscribed {
            repo.getTimer()
                .onEach { produceState(it) } // set mapped states
                .consume(Dispatchers.Default)
        }
        reduce {
            when (it) {
                is CounterIntent.ClickedCounter -> {
                    action(CounterAction.ShowSnackbar(R.string.started_processing))

                    delay(1000)

                    action(CounterAction.ShowSnackbar(R.string.finished_processing))
                    launch {
                        require(Random.nextBoolean()) { "Oops, there was an error in a job" }
                    }
                    updateState<CounterState.DisplayingCounter, _> {
                        copy(counter = counter + 1)
                    }
                }
            }
        }
        recover {
            if (it is IllegalArgumentException)
                send(CounterAction.ShowSnackbar(R.string.error_message))
            else updateState {
                CounterState.Error(it)
            }
            null
        }

        initial(CounterState.Loading)
    }

    private fun Ctx.produceState(timer: Int) = launch {
        updateState {
            // remember that you have to merge states when you are running produceState
            val current = this as? CounterState.DisplayingCounter
            CounterState.DisplayingCounter(timer, current?.counter ?: 0, "TODO: Implement params")
        }
    }
}
