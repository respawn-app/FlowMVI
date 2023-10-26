package pro.respawn.flowmvi.sample.compose

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.plugins.disallowRestartPlugin
import pro.respawn.flowmvi.plugins.platformLoggingPlugin
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.undoRedo
import pro.respawn.flowmvi.plugins.whileSubscribed
import pro.respawn.flowmvi.sample.CounterAction
import pro.respawn.flowmvi.sample.CounterAction.GoBack
import pro.respawn.flowmvi.sample.CounterAction.ShowErrorMessage
import pro.respawn.flowmvi.sample.CounterIntent
import pro.respawn.flowmvi.sample.CounterIntent.ClickedBack
import pro.respawn.flowmvi.sample.CounterIntent.ClickedCounter
import pro.respawn.flowmvi.sample.CounterIntent.ClickedUndo
import pro.respawn.flowmvi.sample.CounterState
import pro.respawn.flowmvi.sample.CounterState.DisplayingCounter
import pro.respawn.flowmvi.sample.repository.CounterRepository
import pro.respawn.flowmvi.util.typed
import kotlin.random.Random

private typealias Ctx = PipelineContext<CounterState, CounterIntent, CounterAction>

class CounterContainer(
    private val repo: CounterRepository,
    private val param: String,
) : Container<CounterState, CounterIntent, CounterAction> {

    override val store = store(CounterState.Loading) {
        name = "Counter"
        install(
            platformLoggingPlugin(),
            disallowRestartPlugin() // store does not restart when it is in a viewmodel
        )
        val undoRedo = undoRedo(10)
        recover {
            undoRedo.reset()
            launch {
                if (it is IllegalArgumentException)
                    action(ShowErrorMessage)
                else updateState {
                    CounterState.Error(it)
                }
            }
            null
        }
        whileSubscribed {
            repo.getTimer()
                .onEach { produceState(it) }
                .consume(Dispatchers.Default)
        }
        reduce {
            when (it) {
                is ClickedUndo -> undoRedo.undo()
                is ClickedBack -> action(GoBack)
                is ClickedCounter -> launch {
                    require(Random.nextBoolean()) { "Oops, there was an error in a job" }
                    undoRedo(
                        redo = {
                            updateState<DisplayingCounter, _> {
                                copy(counter = counter + 1)
                            }
                        },
                        undo = {
                            updateState<DisplayingCounter, _> {
                                copy(counter = counter - 1)
                            }
                        }
                    )
                }
            }
        }
    }

    private suspend fun Ctx.produceState(timer: Int) = updateState {
        // remember that you have to merge states when you are running produceState
        val current = typed<DisplayingCounter>()
        DisplayingCounter(timer, current?.counter ?: 0, param)
    }
}
