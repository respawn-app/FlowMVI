package pro.respawn.flowmvi.sample.provider

import pro.respawn.flowmvi.android.StoreViewModel
import pro.respawn.flowmvi.sample.repo.CounterRepo

class CounterViewModel(
    repo: CounterRepo
) : StoreViewModel<CounterState, CounterIntent, CounterAction>(CounterContainer(repo).store)
