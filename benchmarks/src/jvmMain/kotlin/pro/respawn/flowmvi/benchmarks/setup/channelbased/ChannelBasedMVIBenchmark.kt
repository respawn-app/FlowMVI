package pro.respawn.flowmvi.benchmarks.setup.channelbased

import kotlinx.benchmark.TearDown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import pro.respawn.flowmvi.benchmarks.BenchmarkDefaults
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent
import pro.respawn.flowmvi.benchmarks.setup.optimized.optimizedStore

@Suppress("unused")
@State(Scope.Benchmark)
internal class ChannelBasedMVIBenchmark {

    lateinit var store: ChannelBasedTraditionalStore
    lateinit var scope: CoroutineScope

    @Setup
    fun setup() {
        scope = CoroutineScope(Dispatchers.Unconfined)
        store = ChannelBasedTraditionalStore(scope)
    }

    @Benchmark
    fun benchmark() = runBlocking {
        repeat(BenchmarkDefaults.intentsPerIteration) {
            store.onIntent(BenchmarkIntent.Increment)
        }
        store.state.first { state -> state.counter >= BenchmarkDefaults.intentsPerIteration }
    }

    @TearDown
    fun teardown() = runBlocking {
        scope.cancel()
    }
}
