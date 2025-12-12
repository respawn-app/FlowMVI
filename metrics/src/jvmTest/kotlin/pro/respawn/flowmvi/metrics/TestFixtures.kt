package pro.respawn.flowmvi.metrics

import pro.respawn.flowmvi.logging.StoreLogLevel
import pro.respawn.flowmvi.logging.StoreLogger
import pro.respawn.flowmvi.metrics.api.ActionMetrics
import pro.respawn.flowmvi.metrics.api.ExceptionMetrics
import pro.respawn.flowmvi.metrics.api.IntentMetrics
import pro.respawn.flowmvi.metrics.api.LifecycleMetrics
import pro.respawn.flowmvi.metrics.api.Meta
import pro.respawn.flowmvi.metrics.api.MetricsSnapshot
import pro.respawn.flowmvi.metrics.api.StateMetrics
import pro.respawn.flowmvi.metrics.api.SubscriptionMetrics
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal fun sampleSnapshot(
    meta: Meta = Meta(
        generatedAt = Instant.fromEpochMilliseconds(1_700_000_000_000),
        startTime = Instant.fromEpochMilliseconds(1_699_999_000_000),
        storeName = "demo-store",
        storeId = "demo-store-id",
        runId = "demo-run-id",
        windowSeconds = 60,
        emaAlpha = 0.5f
    ),
): MetricsSnapshot = MetricsSnapshot(
    meta = meta,
    intents = IntentMetrics(
        total = 10,
        processed = 8,
        dropped = 1,
        undelivered = 1,
        opsPerSecond = 2.5,
        durationAvg = 20.milliseconds,
        durationP50 = 10.milliseconds,
        durationP90 = 20.milliseconds,
        durationP95 = 25.milliseconds,
        durationP99 = 30.milliseconds,
        queueTimeAvg = 5.milliseconds,
        inFlightMax = 2,
        interArrivalAvg = 50.milliseconds,
        interArrivalMedian = 40.milliseconds,
        burstMax = 3,
        bufferMaxOccupancy = 5,
        bufferOverflows = 1,
        pluginOverheadAvg = 2.milliseconds,
        pluginOverheadMedian = 1.milliseconds,
    ),
    actions = ActionMetrics(
        sent = 5,
        delivered = 4,
        undelivered = 1,
        opsPerSecond = 1.5,
        deliveryAvg = 30.milliseconds,
        deliveryP50 = 20.milliseconds,
        deliveryP90 = 40.milliseconds,
        deliveryP95 = 50.milliseconds,
        deliveryP99 = 60.milliseconds,
        queueTimeAvg = 6.milliseconds,
        queueTimeMedian = 4.milliseconds,
        bufferMaxOccupancy = 6,
        bufferOverflows = 0,
        pluginOverheadAvg = 1.milliseconds,
        pluginOverheadMedian = 1.milliseconds,
    ),
    state = StateMetrics(
        transitions = 7,
        transitionsVetoed = 1,
        updateAvg = 5.milliseconds,
        updateP50 = 3.milliseconds,
        updateP90 = 7.milliseconds,
        updateP95 = 8.milliseconds,
        updateP99 = 9.milliseconds,
        opsPerSecond = 0.5,
    ),
    subscriptions = SubscriptionMetrics(
        events = 9,
        current = 2,
        peak = 4,
        lifetimeAvg = 30.seconds,
        lifetimeMedian = 20.seconds,
        subscribersAvg = 1.5,
        subscribersMedian = 1.0,
    ),
    lifecycle = LifecycleMetrics(
        startCount = 2,
        stopCount = 1,
        uptimeTotal = 100.seconds,
        lifetimeCurrent = 80.seconds,
        lifetimeAvg = 90.seconds,
        lifetimeMedian = 70.seconds,
        bootstrapAvg = 0.5.seconds,
        bootstrapMedian = 0.25.seconds,
    ),
    exceptions = ExceptionMetrics(
        total = 2,
        handled = 1,
        recoveryLatencyAvg = 15.milliseconds,
        recoveryLatencyMedian = 10.milliseconds,
    ),
)

internal data class LoggedEntry(
    val level: StoreLogLevel,
    val tag: String?,
    val message: String,
)

internal class RecordingLogger : StoreLogger {
    val entries: MutableList<LoggedEntry> = mutableListOf()

    override fun log(level: StoreLogLevel, tag: String?, message: () -> String) {
        entries += LoggedEntry(level, tag, message())
    }
}
