package pro.respawn.flowmvi.benchmarks.setup.metrics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.StateStrategy
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkIntent.Increment
import pro.respawn.flowmvi.benchmarks.setup.BenchmarkState
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateStateImmediate
import pro.respawn.flowmvi.metrics.NoopSink
import pro.respawn.flowmvi.metrics.dsl.collectMetrics
import pro.respawn.flowmvi.metrics.dsl.reportMetrics
import pro.respawn.flowmvi.plugins.reduce

private fun StoreBuilder<*, *, *>.benchmarkConfig() = configure {
    logger = null
    debuggable = false
    actionShareBehavior = ActionShareBehavior.Disabled
    stateStrategy = StateStrategy.Atomic()
    parallelIntents = false
    verifyPlugins = false
    onOverflow = BufferOverflow.SUSPEND
    intentCapacity = Channel.UNLIMITED
}

internal fun baselineStore(scope: CoroutineScope) =
    store<BenchmarkState, BenchmarkIntent, Nothing>(BenchmarkState(), scope) {
        benchmarkConfig()
        reduce {
            when (it) {
                is Increment -> updateState { copy(counter = counter + 1) }
            }
        }
    }

@OptIn(ExperimentalFlowMVIAPI::class)
internal fun metricsStore(
    storeScope: CoroutineScope,
    metricsReportingScope: CoroutineScope,
) = store<BenchmarkState, BenchmarkIntent, Nothing>(BenchmarkState(), storeScope) {
    benchmarkConfig()
    val m = collectMetrics(reportingScope = metricsReportingScope)
    reportMetrics(m, sink = NoopSink())
    reduce {
        when (it) {
            is Increment -> updateState { copy(counter = counter + 1) }
        }
    }
}
