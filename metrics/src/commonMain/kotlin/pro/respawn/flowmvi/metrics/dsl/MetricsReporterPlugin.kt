package pro.respawn.flowmvi.metrics.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.metrics.MetricsSink
import pro.respawn.flowmvi.metrics.api.DefaultMetrics
import pro.respawn.flowmvi.metrics.api.Metrics
import pro.respawn.flowmvi.metrics.api.MetricsSnapshot
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val DefaultName: String = "MetricsReporter"

/**
 * A default [Metrics] collection plugin that periodically snapshots
 * [metrics] and forwards them to [sink], optionally flushing on stop.
 *
 * This plugin supports backpressure. If the [sink] is slow, oldest snapshots will be dropped,
 * reducing frequency of reporting until load normalizes.
 *
 * @param metrics provider that exposes snapshots. A default provider is exposed via [pro.respawn.flowmvi.metrics.dsl.metrics]
 * builder
 * @param offloadScope scope used to launch flushing jobs AFTER store cancellation.
 * This must **NOT** be the store's scope or scope that the store is launched in! See [pro.respawn.flowmvi.metrics.dsl.metrics] for details.
 * @param offloadContext dispatcher for offloaded work
 * @param interval how often to collect snapshots; set to [Duration.INFINITE] or 0 to disable periodic emission
 * @param flushOnStop whether to emit one last snapshot during [StorePlugin.onStop]
 * @param name optional plugin name
 *
 * @param sink destination [pro.respawn.flowmvi.metrics.api.Sink] for snapshots.
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> metricsReporter(
    metrics: Metrics,
    offloadScope: CoroutineScope,
    offloadContext: CoroutineContext = Dispatchers.Default,
    interval: Duration = 30.seconds,
    flushOnStop: Boolean = true,
    name: String? = DefaultName,
    sink: MetricsSink,
): StorePlugin<S, I, A> = plugin {
    this.name = name
    val queue = Channel<MetricsSnapshot>(Channel.BUFFERED, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    if (interval.isPositive() && interval.isFinite()) onStart {
        // sequential offloading w/backpressure
        launch(offloadContext) { for (snapshot in queue) sink.emit(snapshot) }
        // fast snapshotting on interval
        launch(offloadContext) {
            while (isActive) {
                delay(interval)
                queue.send(metrics.snapshot())
            }
        }
    }
    if (flushOnStop) onStop {
        offloadScope.launch(offloadContext) { sink.emit(metrics.snapshot()) }
    }
}

/**
 * A default [Metrics] collection plugin that periodically snapshots
 * [metrics] and forwards them to [sink], optionally flushing on stop.
 *
 * This plugin supports backpressure. If the [sink] is slow, oldest snapshots will be dropped,
 * reducing frequency of reporting until load normalizes.
 *
 * @param metrics provider that exposes snapshots. A default provider is exposed via [pro.respawn.flowmvi.metrics.dsl.metrics]
 * builder
 * @param interval how often to collect snapshots; set to [Duration.INFINITE] or 0 to disable periodic emission
 * @param flushOnStop whether to emit one last snapshot during [StorePlugin.onStop]
 * @param name optional plugin name
 *
 * @param sink destination [pro.respawn.flowmvi.metrics.api.Sink] for snapshots.
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> metricsReporter(
    builder: DefaultMetrics<S, I, A>,
    interval: Duration = 30.seconds,
    flushOnStop: Boolean = true,
    name: String? = DefaultName,
    sink: MetricsSink,
): StorePlugin<S, I, A> = metricsReporter(
    metrics = builder,
    offloadScope = builder.collector.reportingScope,
    offloadContext = builder.collector.offloadContext,
    interval = interval,
    flushOnStop = flushOnStop,
    name = name,
    sink = sink,
)

/**
 * Install a new [metricsReporter] into the current [pro.respawn.flowmvi.api.Store].
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.reportMetrics(
    metrics: Metrics,
    offloadScope: CoroutineScope,
    offloadContext: CoroutineContext = Dispatchers.Default,
    interval: Duration = 30.seconds,
    flushOnStop: Boolean = true,
    name: String? = DefaultName,
    sink: MetricsSink,
): Unit = install(
    metricsReporter(
        metrics = metrics,
        offloadScope = offloadScope,
        interval = interval,
        offloadContext = offloadContext,
        flushOnStop = flushOnStop,
        name = name,
        sink = sink
    )
)

/**
 * Install a new [metricsReporter] into the current [pro.respawn.flowmvi.api.Store].
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.reportMetrics(
    metrics: DefaultMetrics<S, I, A>,
    interval: Duration = 30.seconds,
    flushOnStop: Boolean = true,
    name: String? = DefaultName,
    sink: MetricsSink,
): Unit = install(
    metricsReporter(
        builder = metrics,
        interval = interval,
        flushOnStop = flushOnStop,
        name = name,
        sink = sink
    )
)
