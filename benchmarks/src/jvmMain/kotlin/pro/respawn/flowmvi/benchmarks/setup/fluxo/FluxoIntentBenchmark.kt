package pro.respawn.flowmvi.benchmarks.setup.fluxo

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kt.fluxo.core.annotation.ExperimentalFluxoApi
import kt.fluxo.core.closeAndWait
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads
import pro.respawn.flowmvi.benchmarks.BenchmarkDefaults
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent

@Threads(Threads.MAX)
@Suppress("unused")
@State(Scope.Benchmark)
internal class FluxoIntentBenchmark {

    @OptIn(ExperimentalFluxoApi::class)
    @Benchmark
    fun benchmark() = runBlocking {
        val store = fluxoStore()
        repeat(BenchmarkDefaults.intentsPerIteration) {
            store.send(BenchmarkIntent.Increment)
        }
        store.first { state -> state.counter == BenchmarkDefaults.intentsPerIteration }
        store.closeAndWait()
    }
}
