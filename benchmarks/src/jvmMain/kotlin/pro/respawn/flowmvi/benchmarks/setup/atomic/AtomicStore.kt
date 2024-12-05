package pro.respawn.flowmvi.benchmarks.setup.atomic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent.Increment
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkState
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.reduce

private fun StoreBuilder<*, *, *>.config() = configure {
    logger = null
    debuggable = false
    actionShareBehavior = ActionShareBehavior.Disabled
    atomicStateUpdates = true
    parallelIntents = false
    verifyPlugins = false
    onOverflow = BufferOverflow.SUSPEND
    intentCapacity = Channel.UNLIMITED
}

internal fun atomicParallelStore(
    scope: CoroutineScope
) = store<BenchmarkState, BenchmarkIntent, Nothing>(BenchmarkState(), scope) {
    config()
    reduce {
        when (it) {
            is Increment -> updateState { copy(counter = counter + 1) }
        }
    }
}
