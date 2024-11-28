package pro.respawn.flowmvi.benchmarks.setup.channelbased

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkState

internal class ChannelBasedTraditionalStore(scope: CoroutineScope) {

    private val _state = MutableStateFlow(BenchmarkState())
    val state = _state.asStateFlow()
    val intents = Channel<BenchmarkIntent>()

    init {
        scope.launch {
            for (intent in intents) reduce(intent)
        }
    }

    fun onIntent(intent: BenchmarkIntent) = intents.trySend(intent)

    private fun reduce(intent: BenchmarkIntent) = when (intent) {
        is BenchmarkIntent.Increment -> _state.update { state ->
            state.copy(counter = state.counter + 1)
        }
    }
}
