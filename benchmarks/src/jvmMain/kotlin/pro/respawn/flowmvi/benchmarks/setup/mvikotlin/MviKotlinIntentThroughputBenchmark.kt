package pro.respawn.flowmvi.benchmarks.setup.mvikotlin

import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads
import pro.respawn.flowmvi.benchmarks.BenchmarkDefaults
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent.Increment
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Threads(Threads.MAX)
@Suppress("unused")
@State(Scope.Benchmark)
internal class MviKotlinIntentThroughputBenchmark {

    @Benchmark
    @OperationsPerInvocation(BenchmarkDefaults.intentsPerIteration)
    fun benchmark() = devNullErrorOutput {
        runBlocking {
            val store = mviKotlinCounterStore()
            store.init()

            repeat(BenchmarkDefaults.intentsPerIteration) {
                store.accept(Increment)
            }

            store.states.first { it.counter == BenchmarkDefaults.intentsPerIteration }
            store.dispose()
        }
    }
}

private inline fun devNullErrorOutput(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    // Override stderr to get rid of `[MVIKotlin]: Main thread ID is undefined, main thread assert is disabled`.
    val stderr = System.err
    try {
        System.setErr(java.io.PrintStream(java.io.OutputStream.nullOutputStream()))
        return block()
    } finally {
        System.setErr(stderr)
    }
}
