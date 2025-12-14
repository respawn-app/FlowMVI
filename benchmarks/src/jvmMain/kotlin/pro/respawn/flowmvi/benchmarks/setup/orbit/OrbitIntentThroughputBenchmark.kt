package pro.respawn.flowmvi.benchmarks.setup.orbit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads
import pro.respawn.flowmvi.benchmarks.BenchmarkDefaults

@Threads(Threads.MAX)
@Suppress("unused")
@State(Scope.Benchmark)
internal class OrbitIntentThroughputBenchmark {

    @OperationsPerInvocation(BenchmarkDefaults.intentsPerIteration)
    @Benchmark
    fun benchmark() = runBlocking {
        val scope = CoroutineScope(coroutineContext + SupervisorJob(coroutineContext[Job]))
        val host = OrbitCounterHost(scope)
        repeat(BenchmarkDefaults.intentsPerIteration) { host.increment() }
        host.container.stateFlow.first { it.counter == BenchmarkDefaults.intentsPerIteration }
        host.container.cancel()
    }
}
