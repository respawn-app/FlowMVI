package pro.respawn.flowmvi.benchmarks.setup.parallel

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
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.benchmarks.BenchmarkDefaults
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkState
import pro.respawn.flowmvi.dsl.collect

@Suppress("unused")
@State(Scope.Benchmark)
internal class ParallelFMVIBenchmark {

    lateinit var store: Store<BenchmarkState, BenchmarkIntent, Nothing>
    lateinit var scope: CoroutineScope

    @Setup
    fun setup() = runBlocking {
        scope = CoroutineScope(Dispatchers.Unconfined)
        store = atomicParallelStore(scope)
        store.awaitStartup()
    }

    @Benchmark
    fun benchmark() = runBlocking {
        repeat(BenchmarkDefaults.intentsPerIteration) {
            store.intent(BenchmarkIntent.Increment)
        }
        store.collect { states.first { it.counter >= BenchmarkDefaults.intentsPerIteration } }
    }

    @TearDown
    fun teardown() = runBlocking {
        scope.cancel()
        store.closeAndWait()
    }
}
