package pro.respawn.flowmvi.metrics

import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.decorator.PluginDecorator
import pro.respawn.flowmvi.dsl.StoreBuilder
import kotlin.time.Clock
import kotlin.time.TimeSource

@ExperimentalFlowMVIAPI
@FlowMVIDSL
/**
 * Creates a metrics-collecting decorator for a store.
 * @param reportingScope scope used for offloaded aggregation work
 * @param storeName logical name for tagging metrics
 * @param windowSeconds sliding window length for throughput calculations
 * @param emaAlpha smoothing factor for moving averages
 * @param clock wall-clock source for metadata stamping
 * @param timeSource monotonic time source for durations
 * @param lockEnabled set false for single-threaded UI stores to skip locking
 * @param name decorator name override
 * @param sink sink receiving produced snapshots
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> metricsDecorator(
    reportingScope: CoroutineScope,
    storeName: String? = null,
    windowSeconds: Int = 60,
    emaAlpha: Double = 0.1,
    clock: Clock = Clock.System,
    timeSource: TimeSource = TimeSource.Monotonic,
    lockEnabled: Boolean = true,
    name: String? = "MetricsDecorator",
    sink: MetricsSink,
): PluginDecorator<S, I, A> = MetricsCollector<S, I, A>(
    reportingScope = reportingScope,
    // sink = sink,
    storeName = storeName,
    windowSeconds = windowSeconds,
    emaAlpha = emaAlpha,
    timeSource = timeSource,
    clock = clock,
    lockEnabled = lockEnabled,
).asDecorator(name)

@FlowMVIDSL
@ExperimentalFlowMVIAPI
/**
 * Installs the metrics decorator into a store builder.
 * @param reportingScope scope used for offloaded aggregation work
 * @param storeName logical name for tagging metrics
 * @param windowSeconds sliding window length for throughput calculations
 * @param emaAlpha smoothing factor for moving averages
 * @param clock wall-clock source for metadata stamping
 * @param timeSource monotonic time source for durations
 * @param lockEnabled set false for single-threaded UI stores to skip locking
 * @param name decorator name override
 * @param sink sink receiving produced snapshots
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.collectMetrics(
    reportingScope: CoroutineScope,
    storeName: String? = null,
    windowSeconds: Int = 60,
    emaAlpha: Double = 0.1,
    clock: Clock = Clock.System,
    timeSource: TimeSource = TimeSource.Monotonic,
    lockEnabled: Boolean = true,
    name: String? = "MetricsCollector",
    sink: MetricsSink,
): Unit = install(
    metricsDecorator(
        reportingScope = reportingScope,
        sink = sink,
        storeName = storeName,
        windowSeconds = windowSeconds,
        emaAlpha = emaAlpha,
        timeSource = timeSource,
        clock = clock,
        lockEnabled = lockEnabled,
        name = name
    )
)
