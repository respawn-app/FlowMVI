package pro.respawn.flowmvi.benchmarks.setup.traditional

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads
import pro.respawn.flowmvi.benchmarks.BenchmarkDefaults
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent

@Threads(Threads.MAX)
@Suppress("unused")
@State(Scope.Benchmark)
internal class TraditionalMVIBenchmark {

    @Benchmark
    fun benchmark() = runBlocking {
        val store = TraditionalMVIStore()
        repeat(BenchmarkDefaults.intentsPerIteration) {
            store.onIntent(BenchmarkIntent.Increment)
        }
        store.state.first { state -> state.counter >= BenchmarkDefaults.intentsPerIteration }
    }
}
