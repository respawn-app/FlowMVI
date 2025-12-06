package pro.respawn.flowmvi.metrics.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StoreConfiguration
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Root metrics payload composed of typed sections. Serializable for transport; the in-memory store configuration is
 * kept separately and not encoded.
 */
@Serializable
public data class MetricsSnapshot(
    /** General metadata for this snapshot. */
    val meta: Meta,
    /** Metrics for intent handling. */
    val intents: IntentMetrics,
    /** Metrics for action emission and delivery. */
    val actions: ActionMetrics,
    /** Metrics for state transitions. */
    val state: StateMetrics,
    /** Metrics for subscriptions. */
    val subscriptions: SubscriptionMetrics,
    /** Lifecycle metrics for the store. */
    val lifecycle: LifecycleMetrics,
    /** Exception and recovery metrics. */
    val exceptions: ExceptionMetrics,
    /** In-memory store configuration reference, not serialized. */
    @Transient val storeConfiguration: StoreConfiguration<MVIState>? = null,
)

/** Metadata describing snapshot timing and configuration identity. */
@Serializable
public data class Meta(
    /** Wall-clock moment the snapshot was produced. */
    val generatedAt: Instant,
    /** Logical store name or identifier to tag metrics. */
    val storeName: String? = null,
    /** Optional configuration fingerprint provided by the user. */
    val storeId: String? = null,
    /** Sliding-window length in seconds used for ops/sec and moving stats. */
    val windowSeconds: Int,
    /** EMA smoothing factor used for averages. */
    val emaAlpha: Float,
)

/** Aggregated metrics for intents. */
@Serializable
public data class IntentMetrics(
    /** Total intents received before veto or drop. */
    val total: Long,
    /** Intents that began processing. */
    val processed: Long,
    /** Intents vetoed or dropped before processing. */
    val dropped: Long,
    /** Intents reported undelivered. */
    val undelivered: Long,
    /** Intent processing throughput over the sampling window. */
    val opsPerSecond: Double,
    /** Average end-to-end processing duration. */
    val durationAvg: Duration,
    /** Median processing duration. */
    val durationP50: Duration,
    /** 90th percentile processing duration. */
    val durationP90: Duration,
    /** 95th percentile processing duration. */
    val durationP95: Duration,
    /** 99th percentile processing duration. */
    val durationP99: Duration,
    /** Average time spent queued before processing. */
    val queueTimeAvg: Duration,
    /** Peak concurrent in-flight intents observed. */
    val inFlightMax: Int,
    /** Average gap between consecutive intents. */
    val interArrivalAvg: Duration,
    /** Median gap between consecutive intents. */
    val interArrivalMedian: Duration,
    /** Longest burst of back-to-back intents. */
    val burstMax: Int,
    /** Maximum observed intent buffer occupancy. */
    val bufferMaxOccupancy: Int,
    /** Intent buffer overflow count. */
    val bufferOverflows: Long,
    /** Average time spent in plugins per intent. */
    val pluginOverheadAvg: Duration,
    /** Median time spent in plugins per intent. */
    val pluginOverheadMedian: Duration,
)

/** Aggregated metrics for actions. */
@Serializable
public data class ActionMetrics(
    /** Total actions emitted. */
    val sent: Long,
    /** Actions delivered to at least one subscriber. */
    val delivered: Long,
    /** Actions reported undelivered or dropped. */
    val undelivered: Long,
    /** Action throughput over the sampling window. */
    val opsPerSecond: Double,
    /** Average delivery latency from emit to first subscriber. */
    val deliveryAvg: Duration,
    /** Median delivery latency. */
    val deliveryP50: Duration,
    /** 90th percentile delivery latency. */
    val deliveryP90: Duration,
    /** 95th percentile delivery latency. */
    val deliveryP95: Duration,
    /** 99th percentile delivery latency. */
    val deliveryP99: Duration,
    /** Average time from emit to dispatch. */
    val queueTimeAvg: Duration,
    /** Median time from emit to dispatch. */
    val queueTimeMedian: Duration,
    /** Maximum observed action buffer occupancy. */
    val bufferMaxOccupancy: Int,
    /** Action buffer overflow count. */
    val bufferOverflows: Long,
    /** Average time spent in plugins per action. */
    val pluginOverheadAvg: Duration,
    /** Median time spent in plugins per action. */
    val pluginOverheadMedian: Duration,
)

/** Aggregated metrics for state updates. */
@Serializable
public data class StateMetrics(
    /** Total state transitions attempted. */
    val transitions: Long,
    /** State transitions vetoed or rolled back. */
    val transitionsVetoed: Long,
    /** Average reducer or state update duration. */
    val updateAvg: Duration,
    /** Median reducer or state update duration. */
    val updateP50: Duration,
    /** 90th percentile reducer or state update duration. */
    val updateP90: Duration,
    /** 95th percentile reducer or state update duration. */
    val updateP95: Duration,
    /** 99th percentile reducer or state update duration. */
    val updateP99: Duration,
    /** State transition throughput over the sampling window. */
    val opsPerSecond: Double,
)

/** Aggregated metrics for subscriptions. */
@Serializable
public data class SubscriptionMetrics(
    /** Total subscribe and unsubscribe events. */
    val events: Long,
    /** Current active subscribers. */
    val current: Int,
    /** Peak concurrent subscribers observed. */
    val peak: Int,
    /** Average subscriber lifetime. */
    val lifetimeAvg: Duration,
    /** Median subscriber lifetime. */
    val lifetimeMedian: Duration,
    /** Average subscriber count over the sampling window. */
    val subscribersAvg: Double,
    /** Median subscriber count over the sampling window. */
    val subscribersMedian: Double,
)

/** Lifecycle metrics of the store. */
@Serializable
public data class LifecycleMetrics(
    /** Number of times the store has started. */
    val startCount: Long,
    /** Number of times the store has stopped. */
    val stopCount: Long,
    /** Total uptime accumulated across restarts. */
    val uptimeTotal: Duration,
    /** Current continuous lifetime since last start. */
    val lifetimeCurrent: Duration,
    /** Average continuous lifetime across completed runs. */
    val lifetimeAvg: Duration,
    /** Median continuous lifetime across completed runs. */
    val lifetimeMedian: Duration,
    /** Average bootstrap or onStart latency. */
    val bootstrapAvg: Duration,
    /** Median bootstrap or onStart latency. */
    val bootstrapMedian: Duration,
)

/** Exception handling metrics. */
@Serializable
public data class ExceptionMetrics(
    /** Total exceptions observed by the store. */
    val total: Long,
    /** Exceptions handled or recovered by plugins. */
    val handled: Long,
    /** Average recovery latency for handled exceptions. */
    val recoveryLatencyAvg: Duration,
    /** Median recovery latency for handled exceptions. */
    val recoveryLatencyMedian: Duration,
)
