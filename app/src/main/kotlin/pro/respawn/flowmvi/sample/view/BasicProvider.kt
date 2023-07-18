package pro.respawn.flowmvi.sample.view

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed
import pro.respawn.flowmvi.sample.R
import pro.respawn.flowmvi.sample.di.ProviderClass
import pro.respawn.flowmvi.sample.repo.CounterRepo
import pro.respawn.flowmvi.sample.view.BasicAction.ShowSnackbar
import pro.respawn.flowmvi.sample.view.BasicIntent.ClickedFab
import pro.respawn.flowmvi.sample.view.BasicState.DisplayingCounter

private typealias Ctx = PipelineContext<BasicState, BasicIntent, BasicAction>

class BasicProvider(
    private val param: String,
    private val repo: CounterRepo,
) {

    val store = store<BasicState, BasicIntent, BasicAction>(name, BasicState.Loading) {
        whileSubscribed {
            launchLoadCounter()
        }
        reduce {
            when (it) {
                is ClickedFab -> {
                    send(ShowSnackbar(R.string.started_processing))

                    // Doing long operations will delay intent processing. New intents will NOT result in new coroutines being launched
                    // This means, if we get another intent while delay() is running, it will be processed independently and will start
                    // after this invocation completes.
                    // to solve this, use launch() (example in BaseClassViewModel.kt)
                    delay(1000)

                    send(ShowSnackbar(R.string.finished_processing))
                }
            }

            updateState<DisplayingCounter, _> {
                copy(counter = counter + 1)
            }
        }
    }

    private fun Ctx.launchLoadCounter() = launch {
        val counter = repo.getCounterSync()
        updateState {
            DisplayingCounter(counter, param)
        }
    }

    companion object : ProviderClass()
}
