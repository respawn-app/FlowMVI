package pro.respawn.flowmvi.benchmarks

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent.Increment
import pro.respawn.flowmvi.benchmarks.setup.atomic.atomicParallelStore

fun main() = runBlocking {
    println(ProcessHandle.current().pid())
    val store = atomicParallelStore(this)
    launch {
        while (isActive) {
            store.intent(Increment)
            yield()
        }
    }
    awaitCancellation()
    Unit
}
