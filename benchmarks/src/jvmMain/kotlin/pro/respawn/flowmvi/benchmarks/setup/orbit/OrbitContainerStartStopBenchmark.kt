package pro.respawn.flowmvi.benchmarks.setup.orbit

import kotlinx.benchmark.Benchmark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads

@Threads(Threads.MAX)
@State(Scope.Benchmark)
internal class OrbitContainerStartStopBenchmark {

    @Benchmark
    fun benchmark() = runBlocking {
        val scope = CoroutineScope(coroutineContext + SupervisorJob(coroutineContext[Job]))
        val host = OrbitCounterHost(scope)
        host.touch()
        host.container.cancel()
    }
}
