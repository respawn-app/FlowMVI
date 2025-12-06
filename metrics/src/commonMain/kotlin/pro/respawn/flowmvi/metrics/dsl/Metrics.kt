package pro.respawn.flowmvi.metrics.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.metrics.MetricsCollector
import pro.respawn.flowmvi.metrics.api.MetricsBuilder
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.TimeSource

public fun <S : MVIState, I : MVIIntent, A : MVIAction> metrics(
    reportingScope: CoroutineScope,
    offloadContext: CoroutineContext = Dispatchers.Default,
    windowSeconds: Int = 60,
    emaAlpha: Double = 0.1,
    clock: Clock = Clock.System,
    timeSource: TimeSource = TimeSource.Monotonic,
    lockEnabled: Boolean = true,
): MetricsBuilder<S, I, A> = MetricsBuilder(
    MetricsCollector(
        reportingScope = reportingScope,
        offloadContext = offloadContext,
        windowSeconds = windowSeconds,
        emaAlpha = emaAlpha,
        clock = clock,
        timeSource = timeSource,
        lockEnabled = lockEnabled
    )
)
