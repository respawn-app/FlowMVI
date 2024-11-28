package pro.respawn.flowmvi.benchmarks.setup.traditional

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import pro.respawn.flowmvi.benchmarks.BenchmarkDefaults
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent

@Suppress("unused")
@State(Scope.Benchmark)
internal class TraditionalMVIBenchmark {

    var store = TraditionalMVIStore()

    @Setup
    fun setup() {
        store = TraditionalMVIStore()
    }

    @Benchmark
    fun benchmark() = runBlocking {
        repeat(BenchmarkDefaults.intentsPerIteration) {
            store.onIntent(BenchmarkIntent.Increment)
        }
        store.state.first { state -> state.counter >= BenchmarkDefaults.intentsPerIteration }
    }
}
