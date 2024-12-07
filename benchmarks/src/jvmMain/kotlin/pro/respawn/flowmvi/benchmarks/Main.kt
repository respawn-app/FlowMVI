package pro.respawn.flowmvi.benchmarks

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent.Increment
import pro.respawn.flowmvi.benchmarks.setup.optimized.optimizedStore

fun main() = runBlocking {
    println(ProcessHandle.current().pid())
    val store = optimizedStore(this)
    launch {
        while (isActive) {
            store.intent(Increment)
            yield()
        }
    }
    awaitCancellation()
    Unit
}
