package pro.respawn.flowmvi.benchmarks.setup.fluxo

import kotlinx.benchmark.TearDown
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kt.fluxo.core.closeAndWait
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads
import pro.respawn.flowmvi.benchmarks.BenchmarkDefaults
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkState

@Threads(Threads.MAX)
@Suppress("unused")
@State(Scope.Benchmark)
internal class FluxoIntentBenchmark {

    lateinit var store: kt.fluxo.core.Store<BenchmarkIntent, BenchmarkState>

    @Setup
    fun setup() = runBlocking {
        store = fluxoStore()
    }

    @Benchmark
    fun benchmark() = runBlocking {
        repeat(BenchmarkDefaults.intentsPerIteration) {
            store.send(BenchmarkIntent.Increment)
        }
        store.first { state -> state.counter >= BenchmarkDefaults.intentsPerIteration }
    }

    @TearDown
    fun teardown() = runBlocking {
        store.closeAndWait()
    }
}
