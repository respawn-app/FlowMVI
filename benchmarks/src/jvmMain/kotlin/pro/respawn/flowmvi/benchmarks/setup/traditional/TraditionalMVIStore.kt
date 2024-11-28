package pro.respawn.flowmvi.benchmarks.setup.traditional

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkState

internal class TraditionalMVIStore {

    private val _state = MutableStateFlow(BenchmarkState())
    val state = _state.asStateFlow()

    fun onIntent(intent: BenchmarkIntent) = when (intent) {
        is BenchmarkIntent.Increment -> _state.update { state ->
            state.copy(counter = state.counter + 1)
        }
    }
}
