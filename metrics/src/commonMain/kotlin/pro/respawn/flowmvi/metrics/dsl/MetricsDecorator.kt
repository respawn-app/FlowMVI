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
import pro.respawn.flowmvi.metrics.api.DefaultMetrics
import pro.respawn.flowmvi.metrics.api.Metrics
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

private const val DefaultDecoratorName: String = "MetricsDecorator"

/**
 * Creates a decorator that populates the [pro.respawn.flowmvi.metrics.api.MetricsSnapshot]s
 * returned by the [metrics]. Without this decorator, [Metrics] won't do anything on its own (please see the definition)
 * for more details
 *
 * Install the resulting decorator into a store to start collecting metrics.
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> metricsDecorator(
    metrics: DefaultMetrics<S, I, A>,
    name: String? = DefaultDecoratorName
): PluginDecorator<S, I, A> = metrics.asDecorator(name)

/**
 * Install a new [metricsDecorator]
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.collectMetrics(
    metrics: DefaultMetrics<S, I, A>,
    name: String? = DefaultDecoratorName
): Metrics = metrics.also { install(it.asDecorator(name)) }

/**
 * Create, install, and return a [Metrics] instance that will use the provided configuration to populate and produce
 * [pro.respawn.flowmvi.metrics.api.MetricsSnapshot]s.
 *
 * For the explanation of the parameters, see [metrics].
 *
 * To understand how to collect metrics, see [Metrics] interface definition.
 *
 * Do NOT pass the same or short-lived [reportingScope] to this method. The scope should have a longer lifetime to
 * reliably collect metrics while the store is closed or restarted. More details are in the [metrics] definition
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.collectMetrics(
    reportingScope: CoroutineScope,
    offloadContext: CoroutineContext = Dispatchers.Default,
    windowSeconds: Int = 60,
    emaAlpha: Double = 0.1,
    clock: Clock = Clock.System,
    bucketDuration: Duration = 1.seconds,
    timeSource: TimeSource = TimeSource.Monotonic,
    name: String? = DefaultDecoratorName
): DefaultMetrics<S, I, A> = metrics<S, I, A>(
    reportingScope = reportingScope,
    offloadContext = offloadContext,
    windowSeconds = windowSeconds,
    emaAlpha = emaAlpha,
    bucketDuration = bucketDuration,
    clock = clock,
    timeSource = timeSource
).also { install(it.asDecorator(name)) }
