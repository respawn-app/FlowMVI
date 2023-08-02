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
import pro.respawn.flowmvi.sample.provider.CounterState.DisplayingCounter
import pro.respawn.flowmvi.sample.provider.CounterState.Loading
import pro.respawn.flowmvi.sample.repo.CounterRepo
import kotlin.random.Random

private typealias Ctx = PipelineContext<CounterState, CounterIntent, CounterAction>

class CounterContainer(
    private val repo: CounterRepo,
) {

    val store = store<CounterState, CounterIntent, CounterAction>(Loading) {
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
                    delay(1000)
                    require(Random.nextBoolean()) { "Oops, there was an error in a job" }
                    updateState<DisplayingCounter, _> {
                        copy(counter = counter + 1)
                    }
                }
            }
        }
        recover {
            launch {
                if (it is IllegalArgumentException)
                    send(CounterAction.ShowSnackbar(R.string.error_message))
                else updateState {
                    CounterState.Error(it)
                }
            }
            null
        }
    }

    private suspend fun Ctx.produceState(timer: Int) = updateState {
        // remember that you have to merge states when you are running produceState
        val current = this as? DisplayingCounter
        DisplayingCounter(timer, current?.counter ?: 0, "TODO: Implement params")
    }
}
