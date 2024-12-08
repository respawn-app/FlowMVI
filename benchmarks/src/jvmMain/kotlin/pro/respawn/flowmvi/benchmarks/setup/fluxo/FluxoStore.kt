package pro.respawn.flowmvi.benchmarks.setup.fluxo

import kotlinx.coroutines.Dispatchers
import kt.fluxo.core.annotation.ExperimentalFluxoApi
import kt.fluxo.core.store
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkState

@OptIn(ExperimentalFluxoApi::class)
internal inline fun fluxoStore() = store(BenchmarkState(), reducer = { it: BenchmarkIntent ->
    when (it) {
        BenchmarkIntent.Increment -> copy(counter = counter + 1)
    }
}) {
    coroutineContext = Dispatchers.Unconfined
    intentStrategy = Direct
    debugChecks = false
    lazy = false
}
