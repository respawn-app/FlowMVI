package pro.respawn.flowmvi.sample.view

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import pro.respawn.flowmvi.android.plugins.parcelizeState
import pro.respawn.flowmvi.api.ImmutableContainer
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.intent
import pro.respawn.flowmvi.dsl.lazyStore
import pro.respawn.flowmvi.dsl.reduceLambdas
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.plugins.platformLoggingPlugin
import pro.respawn.flowmvi.plugins.whileSubscribed
import pro.respawn.flowmvi.sample.BuildConfig
import pro.respawn.flowmvi.sample.CounterAction
import pro.respawn.flowmvi.sample.CounterAction.ShowLambdaMessage
import pro.respawn.flowmvi.sample.CounterLambdaIntent
import pro.respawn.flowmvi.sample.CounterState
import pro.respawn.flowmvi.sample.CounterState.DisplayingCounter
import pro.respawn.flowmvi.sample.repository.CounterRepository
import pro.respawn.flowmvi.util.typed

private typealias Ctx = PipelineContext<CounterState, CounterLambdaIntent, CounterAction>

class LambdaViewModel(
    repo: CounterRepository,
    savedStateHandle: SavedStateHandle,
    private val param: String,
) : ViewModel(), ImmutableContainer<CounterState, CounterLambdaIntent, CounterAction> {

    override val store by lazyStore(
        initial = CounterState.Loading,
        scope = viewModelScope,
    ) {
        name = "Counter"
        debuggable = BuildConfig.DEBUG
        install(platformLoggingPlugin())
        whileSubscribed {
            repo.getTimer()
                .onEach { produceState(it) } // set mapped states
                .consume(Dispatchers.Default)
        }
        reduceLambdas()
    }

    private suspend fun Ctx.produceState(timer: Int) = updateState {
        // remember that you have to merge states when you are running produceState
        val current = typed<DisplayingCounter>()
        DisplayingCounter(timer, current?.counter ?: 0, param)
    }

    fun onClickCounter() = store.intent {
        action(ShowLambdaMessage)
        updateState<DisplayingCounter, _> {
            copy(counter = counter + 1)
        }
    }
}
