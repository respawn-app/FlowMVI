package pro.respawn.flowmvi.benchmarks.setup.metrics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.annotations.Threads
import org.openjdk.jmh.annotations.OperationsPerInvocation
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.benchmarks.BenchmarkDefaults
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent.Increment
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkState
import pro.respawn.flowmvi.dsl.collect

@Threads(Threads.MAX)
@Suppress("unused")
@State(Scope.Benchmark)
internal class MetricsOverheadBenchmark {

    private val reportingScope = CoroutineScope(SupervisorJob() + Dispatchers.Default.limitedParallelism(1))

    @OperationsPerInvocation(BenchmarkDefaults.intentsPerIteration)
    @Benchmark
    fun baseline() = runBlocking {
        val store = baselineStore(this)
        runBenchmark(store)
    }

    @OperationsPerInvocation(BenchmarkDefaults.intentsPerIteration)
    @Benchmark
    fun withMetrics() = runBlocking {
        val store = metricsStore(
            storeScope = this,
            metricsReportingScope = reportingScope,
        )
        runBenchmark(store)
    }

    @TearDown
    fun tearDown() = reportingScope.cancel()

    private suspend fun runBenchmark(store: Store<BenchmarkState, BenchmarkIntent, Nothing>) {
        repeat(BenchmarkDefaults.intentsPerIteration) {
            store.intent(Increment)
        }
        store.collect {
            states.first { state -> state.counter == BenchmarkDefaults.intentsPerIteration }
        }
        store.closeAndWait()
    }
}
