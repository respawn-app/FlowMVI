package pro.respawn.flowmvi.benchmarks

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent.Increment
import pro.respawn.flowmvi.benchmarks.setup.atomic.atomicStore

/**
 * run an infinite process for profiling
 */
fun main(): Unit = runBlocking {
    println(ProcessHandle.current().pid())
    val store = atomicStore(this)
    launch {
        while (isActive) {
            store.intent(Increment)
            yield()
        }
    }
    awaitCancellation()
}
