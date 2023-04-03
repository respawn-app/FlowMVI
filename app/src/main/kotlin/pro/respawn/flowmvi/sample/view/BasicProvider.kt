package pro.respawn.flowmvi.sample.view

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import pro.respawn.flowmvi.provider.StoreProvider
import pro.respawn.flowmvi.sample.ProviderClass
import pro.respawn.flowmvi.sample.R
import pro.respawn.flowmvi.sample.repo.CounterRepo
import pro.respawn.flowmvi.updateState

class BasicProvider(
    private val param: String,
    private val repo: CounterRepo,
) : StoreProvider<BasicState, BasicIntent, BasicAction>(BasicState.Loading) {

    companion object : ProviderClass<BasicState, BasicIntent, BasicAction>()

    override fun CoroutineScope.onStart() {
        launchLoadCounter()
    }

    override suspend fun CoroutineScope.reduce(intent: BasicIntent) {
        when (intent) {
            is BasicIntent.ClickedFab -> {
                send(BasicAction.ShowSnackbar(R.string.started_processing))

                // Doing long operations will delay intent processing. New intents will NOT result in new coroutines being launched
                // This means, if we get another intent while delay() is running, it will be processed independently and will start
                // after this invocation completes.
                // to solve this, use launchRecovering() (example in BaseClassViewModel.kt)
                delay(1000)

                send(BasicAction.ShowSnackbar(R.string.finished_processing))
            }
        }

        updateState<BasicState.DisplayingCounter, _> {
            copy(counter = counter + 1)
        }
    }

    private fun CoroutineScope.launchLoadCounter() = launchRecovering {
        val counter = repo.getCounterSync()
        updateState {
            BasicState.DisplayingCounter(counter, param)
        }
    }
}
