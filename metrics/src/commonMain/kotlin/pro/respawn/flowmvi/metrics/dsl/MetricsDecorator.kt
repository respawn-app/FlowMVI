package pro.respawn.flowmvi.metrics.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.decorator.PluginDecorator
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.metrics.api.Metrics
import pro.respawn.flowmvi.metrics.api.MetricsBuilder
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.TimeSource

private const val DefaultDecoratorName: String = "MetricsDecorator"

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> metricsDecorator(
    factory: MetricsBuilder<S, I, A>,
    name: String? = DefaultDecoratorName
): PluginDecorator<S, I, A> = factory.asDecorator(name)

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.collectMetrics(
    factory: MetricsBuilder<S, I, A>,
    name: String? = DefaultDecoratorName
): Metrics = factory.also { install(it.asDecorator(name)) }

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.collectMetrics(
    reportingScope: CoroutineScope,
    offloadContext: CoroutineContext = Dispatchers.Default,
    windowSeconds: Int = 60,
    emaAlpha: Double = 0.1,
    clock: Clock = Clock.System,
    timeSource: TimeSource = TimeSource.Monotonic,
    lockEnabled: Boolean = true,
    name: String? = DefaultDecoratorName
): Metrics = metrics<S, I, A>(
    reportingScope = reportingScope,
    offloadContext = offloadContext,
    windowSeconds = windowSeconds,
    emaAlpha = emaAlpha,
    clock = clock,
    timeSource = timeSource,
    lockEnabled = lockEnabled
).also { install(it.asDecorator(name)) }
