package pro.respawn.flowmvi.sample.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import pro.respawn.flowmvi.android.plugins.androidLoggingPlugin
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.LambdaIntent
import pro.respawn.flowmvi.dsl.reduceLambdas
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.whileSubscribed
import pro.respawn.flowmvi.sample.repo.CounterRepo

private typealias Context = PipelineContext<CounterState, LambdaIntent<CounterState, CounterAction>, CounterAction>

class LambdaCounterProvider(
    private val repo: CounterRepo,
) {

    val store = store<CounterState, LambdaIntent<CounterState, CounterAction>, CounterAction> {
        name = "Counter"
        install(androidLoggingPlugin())
        reduceLambdas()
        whileSubscribed {
            repo.getTimer()
                .onEach { produceState(it) } // set mapped states
                .consume(Dispatchers.Default)
        }
        initial(CounterState.Loading)
    }

    private suspend fun Context.produceState(timer: Int) = updateState {
        // remember that you have to merge states when you are running produceState
        val current = this as? CounterState.DisplayingCounter
        CounterState.DisplayingCounter(timer, current?.counter ?: 0, "TODO: Implement params")
    }
}
