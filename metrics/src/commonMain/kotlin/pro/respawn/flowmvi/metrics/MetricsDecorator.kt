package pro.respawn.flowmvi.metrics

import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.decorator.PluginDecorator
import pro.respawn.flowmvi.dsl.StoreBuilder
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Clock
import kotlin.time.TimeSource

@ExperimentalFlowMVIAPI
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> metricsDecorator(
    reportingScope: CoroutineScope,
    storeName: String? = null,
    windowSeconds: Int = 60,
    emaAlpha: Double = 0.1,
    clock: Clock = Clock.System,
    timeSource: TimeSource = TimeSource.Monotonic,
    name: String? = "MetricsDecorator",
    sink: MetricsSink,
): PluginDecorator<S, I, A> = MetricsCollector<S, I, A>(
    reportingScope = reportingScope,
    sink = sink,
    storeName = storeName,
    windowSeconds = windowSeconds,
    emaAlpha = emaAlpha,
    timeSource = timeSource,
    clock = clock
).asDecorator(name)

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.collectMetrics(
    reportingScope: CoroutineScope,
    storeName: String? = null,
    windowSeconds: Int = 60,
    emaAlpha: Double = 0.1,
    clock: Clock = Clock.System,
    timeSource: TimeSource = TimeSource.Monotonic,
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
        name = name
    )
)
