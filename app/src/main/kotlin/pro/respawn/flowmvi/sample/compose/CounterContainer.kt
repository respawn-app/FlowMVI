package pro.respawn.flowmvi.sample.compose

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.debugger.plugin.enableRemoteDebugging
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.plugins.disallowRestart
import pro.respawn.flowmvi.plugins.enableLogging
import pro.respawn.flowmvi.plugins.manageJobs
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.registerOrReplace
import pro.respawn.flowmvi.plugins.undoRedo
import pro.respawn.flowmvi.plugins.whileSubscribed
import pro.respawn.flowmvi.sample.BuildConfig
import pro.respawn.flowmvi.sample.CounterAction
import pro.respawn.flowmvi.sample.CounterAction.GoBack
import pro.respawn.flowmvi.sample.CounterAction.ShowErrorMessage
import pro.respawn.flowmvi.sample.CounterIntent
import pro.respawn.flowmvi.sample.CounterIntent.ClickedBack
import pro.respawn.flowmvi.sample.CounterIntent.ClickedCounter
import pro.respawn.flowmvi.sample.CounterIntent.ClickedUndo
import pro.respawn.flowmvi.sample.CounterIntent.InputChanged
import pro.respawn.flowmvi.sample.CounterState
import pro.respawn.flowmvi.sample.CounterState.DisplayingCounter
import pro.respawn.flowmvi.sample.repository.CounterRepository
import pro.respawn.flowmvi.savedstate.api.NullRecover
import pro.respawn.flowmvi.savedstate.plugins.serializeState
import pro.respawn.flowmvi.util.typed
import kotlin.random.Random

private typealias Ctx = PipelineContext<CounterState, CounterIntent, CounterAction>

class CounterContainer(
    private val repo: CounterRepository,
    private val param: String,
    private val json: Json,
    context: Context,
) : Container<CounterState, CounterIntent, CounterAction> {

    private val cacheDir = context.cacheDir.resolve("state").path

    override val store = store(CounterState.Loading) {
        name = "CounterContainer"
        debuggable = BuildConfig.DEBUG
        if (debuggable) {
            enableRemoteDebugging()
            enableLogging()
        }
        disallowRestart() // store does not restart in a ViewModel

        serializeState(
            dir = cacheDir,
            json = json,
            serializer = DisplayingCounter.serializer(),
            recover = NullRecover,
        )
        val undoRedo = undoRedo(10)
        val jobManager = manageJobs()
        recover {
            if (it is IllegalArgumentException)
                action(ShowErrorMessage(it.message))
            else updateState {
                jobManager.cancelAndJoin("timer")
                CounterState.Error(it)
            }
            null
        }
        whileSubscribed {
            launch {
                repo.getTimer()
                    .onEach { produceState(it) }
                    .consume(Dispatchers.Default)
            }.apply {
                registerOrReplace(jobManager, "timer")
                join()
            }
        }
        reduce {
            when (it) {
                is ClickedUndo -> undoRedo.undo()
                is ClickedBack -> action(GoBack)
                is InputChanged -> updateState<DisplayingCounter, _> {
                    copy(input = it.value)
                }
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
        //  merge states
        val current = typed<DisplayingCounter>()
        DisplayingCounter(
            timer = timer,
            counter = current?.counter ?: 0,
            input = current?.input ?: param
        )
    }
}
