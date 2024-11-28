package pro.respawn.flowmvi.benchmarks.setup.fluxo

import kotlinx.coroutines.Dispatchers
import kt.fluxo.core.annotation.ExperimentalFluxoApi
import kt.fluxo.core.store
import kt.fluxo.core.updateState
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkState

@OptIn(ExperimentalFluxoApi::class)
internal inline fun fluxoStore(
) = store(BenchmarkState(), handler = { it: BenchmarkIntent ->
    updateState { it.copy(counter = it.counter + 1) }
}) {
    coroutineContext = Dispatchers.Unconfined
    intentStrategy = Direct
    debugChecks = false
    lazy = false
}
