package pro.respawn.flowmvi.benchmarks.setup.optimized

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import pro.respawn.flowmvi.api.ActionShareBehavior.Disabled
import pro.respawn.flowmvi.api.StateStrategy.Immediate
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent.Increment
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkState
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateStateImmediate
import pro.respawn.flowmvi.plugins.reducePlugin

internal fun StoreBuilder<*, *, *>.config() {
    configure {
        logger = null
        debuggable = false
        actionShareBehavior = Disabled
        stateStrategy = Immediate
        parallelIntents = false
        verifyPlugins = false
        onOverflow = BufferOverflow.SUSPEND
        intentCapacity = Channel.UNLIMITED
    }
}

private val reduce = reducePlugin<BenchmarkState, BenchmarkIntent, Nothing> {
    when (it) {
        is Increment -> updateStateImmediate {
            copy(counter = counter + 1)
        }
    }
}

internal inline fun optimizedStore(
    scope: CoroutineScope,
) = store<BenchmarkState, BenchmarkIntent, Nothing>(BenchmarkState(), scope) {
    config()
    install(reduce)
}

internal inline fun optimizedStore() = store<BenchmarkState, BenchmarkIntent, Nothing>(BenchmarkState()) {
    config()
    install(reduce)
}
