package pro.respawn.flowmvi.benchmarks.setup.optimized

import kotlinx.benchmark.Benchmark
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads

@Threads(Threads.MAX)
@State(Scope.Benchmark)
internal class OptimizedFMVIStartStopBenchmark {

    @Benchmark
    fun benchmark() = runBlocking {
        val store = optimizedStore()
        store.start(this).awaitStartup()
        store.closeAndWait()
    }
}
