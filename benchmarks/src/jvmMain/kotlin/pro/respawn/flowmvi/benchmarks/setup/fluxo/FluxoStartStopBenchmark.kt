package pro.respawn.flowmvi.benchmarks.setup.fluxo

import kotlinx.benchmark.Benchmark
import kotlinx.coroutines.runBlocking
import kt.fluxo.core.annotation.ExperimentalFluxoApi
import kt.fluxo.core.closeAndWait
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads

@Threads(Threads.MAX)
@Suppress("unused")
@State(Scope.Benchmark)
class FluxoStartStopBenchmark {

    @OptIn(ExperimentalFluxoApi::class)
    @Benchmark
    fun benchmark() = runBlocking {
        val store = fluxoStore()
        store.start().join()
        store.closeAndWait()
    }
}
