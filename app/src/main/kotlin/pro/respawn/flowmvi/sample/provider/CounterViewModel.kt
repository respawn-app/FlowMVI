package pro.respawn.flowmvi.sample.provider

import pro.respawn.flowmvi.android.StoreViewModel
import pro.respawn.flowmvi.dsl.LambdaIntent
import pro.respawn.flowmvi.dsl.send
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.sample.R
import pro.respawn.flowmvi.sample.repo.CounterRepo

class CounterViewModel(
    repo: CounterRepo
) : StoreViewModel<CounterState, CounterIntent, CounterAction>(CounterProvider(repo).store)

class LambdaViewModel(
    repo: CounterRepo
) : StoreViewModel<CounterState, LambdaIntent<CounterState, CounterAction>, CounterAction>(
    LambdaCounterProvider(repo).store
) {

    fun onClickCounter() = send {
        action(CounterAction.ShowSnackbar(R.string.lambda_processing))
        updateState<CounterState.DisplayingCounter, _> {
            copy(counter = counter + 1)
        }
    }
}
