package pro.respawn.flowmvi.metrics.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.metrics.MetricsCollector
import pro.respawn.flowmvi.metrics.api.DefaultMetrics
import pro.respawn.flowmvi.metrics.api.Metrics
import pro.respawn.flowmvi.metrics.api.MetricsSnapshot
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Create a [DefaultMetrics].
 *
 * The returned [Metrics] implementation must be used with the [metricsDecorator] to actually
 * populate the [Metrics.snapshot] return value.
 *
 * @param reportingScope scope used for offloaded metric reporting.
 * This scope **MUST** outlive the store's scope, because if the store is stopped, the [MetricsSnapshot] returned will
 * become stale! A more long-lived scope, such as an application, process, or component scope is recommended.
 * When [metricsDecorator] runs, it only collects metrics when the store is running, so the target store can be safely
 * stopped. But the scope passed here is needed to ensure metrics are drained reliably even after the store's EoL.
 * @param offloadContext dispatcher for computationally-intensive work, a non-main thread dispatcher is recommended.
 * @param windowSeconds length of the sliding window used for rate calculations.
 * @param emaAlpha smoothing factor for [EMA](https://en.wikipedia.org/wiki/Moving_average) calculations.
 * @param clock wall-clock provider for timestamps.
 * @param timeSource monotonic time source for duration retrieval.
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> metrics(
    reportingScope: CoroutineScope,
    offloadContext: CoroutineContext = Dispatchers.Default,
    windowSeconds: Int = 60,
    emaAlpha: Double = 0.1,
    bucketDuration: Duration = 1.seconds,
    clock: Clock = Clock.System,
    timeSource: TimeSource = TimeSource.Monotonic,
): DefaultMetrics<S, I, A> = DefaultMetrics(
    MetricsCollector(
        reportingScope = reportingScope,
        offloadContext = offloadContext,
        windowSeconds = windowSeconds,
        emaAlpha = emaAlpha,
        clock = clock,
        timeSource = timeSource,
        bucketDuration = bucketDuration,
    )
)
