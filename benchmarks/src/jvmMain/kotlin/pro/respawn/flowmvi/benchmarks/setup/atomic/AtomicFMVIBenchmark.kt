package pro.respawn.flowmvi.benchmarks.setup.atomic

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads
import pro.respawn.flowmvi.benchmarks.BenchmarkDefaults
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent
import pro.respawn.flowmvi.dsl.collect

@Threads(Threads.MAX)
@Suppress("unused")
@State(Scope.Benchmark)
internal class AtomicFMVIBenchmark {

    @Benchmark
    fun benchmark() = runBlocking {
        val store = atomicStore(this)
        repeat(BenchmarkDefaults.intentsPerIteration) {
            store.emit(BenchmarkIntent.Increment)
        }
        store.collect {
            states.first { state -> state.counter == BenchmarkDefaults.intentsPerIteration }
        }
        store.closeAndWait()
    }
}
