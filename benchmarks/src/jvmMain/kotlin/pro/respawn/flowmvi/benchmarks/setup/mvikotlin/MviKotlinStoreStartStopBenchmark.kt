package pro.respawn.flowmvi.benchmarks.setup.mvikotlin

import kotlinx.benchmark.Benchmark
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads

@Threads(Threads.MAX)
@State(Scope.Benchmark)
internal class MviKotlinStoreStartStopBenchmark {

    @Benchmark
    fun benchmark() = runBlocking {
        val store = mviKotlinCounterStore()
        store.init()
        store.dispose()
    }
}
