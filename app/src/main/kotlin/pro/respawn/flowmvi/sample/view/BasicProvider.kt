package pro.respawn.flowmvi.sample.view

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import pro.respawn.flowmvi.provider.StoreProvider
import pro.respawn.flowmvi.sample.R
import pro.respawn.flowmvi.sample.di.ProviderClass
import pro.respawn.flowmvi.sample.repo.CounterRepo
import pro.respawn.flowmvi.sample.view.BasicAction.ShowSnackbar
import pro.respawn.flowmvi.sample.view.BasicIntent.ClickedFab
import pro.respawn.flowmvi.sample.view.BasicState.DisplayingCounter
import pro.respawn.flowmvi.sample.view.BasicState.Loading

class BasicProvider(
    private val param: String,
    private val repo: CounterRepo,
) : StoreProvider<BasicState, BasicIntent, BasicAction>(Loading) {

    companion object : ProviderClass<BasicState, BasicIntent, BasicAction>()

    override fun CoroutineScope.onStart() {
        launchLoadCounter()
    }

    override suspend fun CoroutineScope.reduce(intent: BasicIntent) {
        when (intent) {
            is ClickedFab -> {
                send(ShowSnackbar(R.string.started_processing))

                // Doing long operations will delay intent processing. New intents will NOT result in new coroutines being launched
                // This means, if we get another intent while delay() is running, it will be processed independently and will start
                // after this invocation completes.
                // to solve this, use launchRecovering() (example in BaseClassViewModel.kt)
                delay(1000)

                send(ShowSnackbar(R.string.finished_processing))
            }
        }

        updateState<DisplayingCounter> {
            copy(counter = counter + 1)
        }
    }

    private fun CoroutineScope.launchLoadCounter() = launchRecovering {
        val counter = repo.getCounterSync()
        updateState {
            DisplayingCounter(counter, param)
        }
    }
}
