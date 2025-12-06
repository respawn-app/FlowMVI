package pro.respawn.flowmvi.metrics.dsl

import kotlinx.coroutines.CoroutineScope
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
import pro.respawn.flowmvi.metrics.api.Metrics
import pro.respawn.flowmvi.metrics.api.MetricsBuilder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val DefaultName: String = "MetricsReporter"

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> metricsReporter(
    metrics: Metrics,
    offloadScope: CoroutineScope,
    interval: Duration = 30.seconds,
    flushOnStop: Boolean = true,
    name: String? = DefaultName,
    sink: MetricsSink,
): StorePlugin<S, I, A> = plugin {
    this.name = name
    if (interval.isPositive() && interval.isFinite()) onStart {
        launch {
            while (isActive) {
                delay(interval)
                sink.emit(metrics.snapshot())
            }
        }
    }
    if (flushOnStop) onStop {
        offloadScope.launch { sink.emit(metrics.snapshot()) }
    }
}

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> metricsReporter(
    builder: MetricsBuilder<S, I, A>,
    interval: Duration = 30.seconds,
    flushOnStop: Boolean = true,
    name: String? = DefaultName,
    sink: MetricsSink,
): StorePlugin<S, I, A> = metricsReporter(
    metrics = builder,
    offloadScope = builder.collector.reportingScope,
    interval = interval,
    flushOnStop = flushOnStop,
    name = name,
    sink = sink
)

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.reportMetrics(
    metrics: Metrics,
    offloadScope: CoroutineScope,
    interval: Duration = 30.seconds,
    flushOnStop: Boolean = true,
    name: String? = DefaultName,
    sink: MetricsSink,
): Unit = install(
    metricsReporter(
        metrics = metrics,
        offloadScope = offloadScope,
        interval = interval,
        flushOnStop = flushOnStop,
        name = name,
        sink = sink
    )
)

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.reportMetrics(
    builder: MetricsBuilder<S, I, A>,
    interval: Duration = 30.seconds,
    flushOnStop: Boolean = true,
    name: String? = DefaultName,
    sink: MetricsSink,
): Unit = install(
    metricsReporter(
        builder = builder,
        interval = interval,
        flushOnStop = flushOnStop,
        name = name,
        sink = sink
    )
)
