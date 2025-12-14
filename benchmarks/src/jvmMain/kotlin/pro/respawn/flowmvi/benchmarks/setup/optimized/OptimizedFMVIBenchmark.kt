package pro.respawn.flowmvi.benchmarks.setup.optimized

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads
import pro.respawn.flowmvi.benchmarks.BenchmarkDefaults
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent
import pro.respawn.flowmvi.dsl.collect

@Threads(Threads.MAX)
@Suppress("unused")
@State(Scope.Benchmark)
internal class OptimizedFMVIBenchmark {

    @Benchmark
    @OperationsPerInvocation(BenchmarkDefaults.intentsPerIteration)
    fun benchmark() = runBlocking {
        val store = optimizedStore(this)
        repeat(BenchmarkDefaults.intentsPerIteration) {
            store.intent(BenchmarkIntent.Increment)
        }
        store.collect { states.first { it.counter == BenchmarkDefaults.intentsPerIteration } }
        store.closeAndWait()
    }
}
