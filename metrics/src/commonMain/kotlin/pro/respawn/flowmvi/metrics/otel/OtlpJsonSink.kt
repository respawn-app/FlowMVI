package pro.respawn.flowmvi.metrics.otel

import kotlinx.serialization.json.Json
import pro.respawn.flowmvi.metrics.MappingSink
import pro.respawn.flowmvi.metrics.MetricsSink
import pro.respawn.flowmvi.metrics.Quantile
import pro.respawn.flowmvi.metrics.api.ActionMetrics
import pro.respawn.flowmvi.metrics.api.ExceptionMetrics
import pro.respawn.flowmvi.metrics.api.IntentMetrics
import pro.respawn.flowmvi.metrics.api.LifecycleMetrics
import pro.respawn.flowmvi.metrics.api.Meta
import pro.respawn.flowmvi.metrics.api.MetricsSnapshot
import pro.respawn.flowmvi.metrics.api.Sink
import pro.respawn.flowmvi.metrics.api.StateMetrics
import pro.respawn.flowmvi.metrics.api.SubscriptionMetrics
import kotlin.time.Duration
import kotlin.time.Instant

private const val DEFAULT_NAMESPACE: String = "flowmvi"
private const val DEFAULT_SCOPE: String = "flowmvi.metrics"
private const val SECONDS_UNIT: String = "s"
private const val NANOS_PER_MILLI: Long = 1_000_000
private const val NANOS_IN_SECOND: Double = 1_000_000_000.0

private val DefaultJson: Json = Json {
    encodeDefaults = false
    allowSpecialFloatingPointValues = true
}

/** Maps a [MetricsSnapshot] to an OTLP JSON payload ready for serialization. */
public fun MetricsSnapshot.toOtlpPayload(
    namespace: String = DEFAULT_NAMESPACE,
    resourceAttributes: Map<String, String> = emptyMap(),
    scopeName: String = DEFAULT_SCOPE,
    temporality: AggregationTemporality = AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE,
): OtlpMetricsPayload {
    val timestamp = meta.generatedAt.toEpochNanoseconds()
    val attributes = baseAttributes(meta, resourceAttributes)
    return OtlpMetricsPayload(
        resourceMetrics = listOf(
            OtlpResourceMetrics(
                resource = OtlpResource(attributes),
                scopeMetrics = listOf(
                    OtlpScopeMetrics(
                        scope = OtlpInstrumentationScope(name = scopeName),
                        metrics = metrics(
                            snapshot = this,
                            namespace = namespace,
                            attributes = attributes,
                            timestamp = timestamp,
                            temporality = temporality
                        )
                    )
                )
            )
        )
    )
}

/** Builds a sink that emits OTLP JSON (metrics) strings. */
public fun OtlpJsonMetricsSink(
    delegate: Sink<String>,
    namespace: String = DEFAULT_NAMESPACE,
    scopeName: String = DEFAULT_SCOPE,
    resourceAttributes: Map<String, String> = emptyMap(),
    temporality: AggregationTemporality = AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE,
    json: Json = DefaultJson,
): MetricsSink = MappingSink(delegate) { snapshot ->
    val payload = snapshot.toOtlpPayload(
        namespace = namespace,
        resourceAttributes = resourceAttributes,
        scopeName = scopeName,
        temporality = temporality
    )
    json.encodeToString(OtlpMetricsPayload.serializer(), payload)
}

private data class QuantileSpec<T>(val quantile: Quantile, val accessor: (T) -> Duration)

private val INTENT_QUANTILES: List<QuantileSpec<IntentMetrics>> = listOf(
    QuantileSpec(Quantile.Q50) { it.durationP50 },
    QuantileSpec(Quantile.Q90) { it.durationP90 },
    QuantileSpec(Quantile.Q95) { it.durationP95 },
    QuantileSpec(Quantile.Q99) { it.durationP99 },
)

private val ACTION_QUANTILES: List<QuantileSpec<ActionMetrics>> = listOf(
    QuantileSpec(Quantile.Q50) { it.deliveryP50 },
    QuantileSpec(Quantile.Q90) { it.deliveryP90 },
    QuantileSpec(Quantile.Q95) { it.deliveryP95 },
    QuantileSpec(Quantile.Q99) { it.deliveryP99 },
)

private val STATE_QUANTILES: List<QuantileSpec<StateMetrics>> = listOf(
    QuantileSpec(Quantile.Q50) { it.updateP50 },
    QuantileSpec(Quantile.Q90) { it.updateP90 },
    QuantileSpec(Quantile.Q95) { it.updateP95 },
    QuantileSpec(Quantile.Q99) { it.updateP99 },
)

private fun metrics(
    snapshot: MetricsSnapshot,
    namespace: String,
    attributes: List<OtlpKeyValue>,
    timestamp: Long,
    temporality: AggregationTemporality,
): List<OtlpMetric> = buildList {
    addAll(configMetrics(snapshot, namespace, attributes, timestamp))
    addAll(intentMetrics(snapshot.intents, namespace, attributes, timestamp, temporality))
    addAll(actionMetrics(snapshot.actions, namespace, attributes, timestamp, temporality))
    addAll(stateMetrics(snapshot.state, namespace, attributes, timestamp, temporality))
    addAll(subscriptionMetrics(snapshot.subscriptions, namespace, attributes, timestamp, temporality))
    addAll(lifecycleMetrics(snapshot.lifecycle, namespace, attributes, timestamp, temporality))
    addAll(exceptionMetrics(snapshot.exceptions, namespace, attributes, timestamp, temporality))
}

private fun configMetrics(
    snapshot: MetricsSnapshot,
    namespace: String,
    attributes: List<OtlpKeyValue>,
    timestamp: Long,
): List<OtlpMetric> = listOf(
    gauge(
        namespace = namespace,
        name = "config_window_seconds",
        description = "Metrics sampling window length",
        value = snapshot.meta.windowSeconds.toDouble(),
        attributes = attributes,
        timestamp = timestamp
    ),
    gauge(
        namespace = namespace,
        name = "config_ema_alpha",
        description = "Metrics EMA smoothing factor",
        value = snapshot.meta.emaAlpha.toDouble(),
        attributes = attributes,
        timestamp = timestamp
    ),
)

@Suppress("LongMethod")
private fun intentMetrics(
    intents: IntentMetrics,
    namespace: String,
    attributes: List<OtlpKeyValue>,
    timestamp: Long,
    temporality: AggregationTemporality,
): List<OtlpMetric> = listOf(
    counter(
        namespace = namespace,
        name = "intents_total",
        description = "Total intents",
        value = intents.total,
        attributes = attributes,
        timestamp = timestamp,
        temporality = temporality
    ),
    counter(
        namespace = namespace,
        name = "intents_processed_total",
        description = "Processed intents",
        value = intents.processed,
        attributes = attributes,
        timestamp = timestamp,
        temporality = temporality
    ),
    counter(
        namespace = namespace,
        name = "intents_dropped_total",
        description = "Dropped intents",
        value = intents.dropped,
        attributes = attributes,
        timestamp = timestamp,
        temporality = temporality
    ),
    counter(
        namespace = namespace,
        name = "intents_undelivered_total",
        description = "Undelivered intents",
        value = intents.undelivered,
        attributes = attributes,
        timestamp = timestamp,
        temporality = temporality
    ),
    gauge(
        namespace = namespace,
        name = "intents_ops_per_second",
        description = "Intent throughput",
        value = intents.opsPerSecond,
        attributes = attributes,
        timestamp = timestamp
    ),
    gauge(
        namespace = namespace,
        name = "intents_duration_seconds_avg",
        description = "Average intent duration",
        value = intents.durationAvg.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    quantileGauge(
        namespace = namespace,
        name = "intents_duration_seconds",
        description = "Intent duration quantiles",
        quantiles = INTENT_QUANTILES,
        source = intents,
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    gauge(
        namespace = namespace,
        name = "intents_queue_time_seconds_avg",
        description = "Average intent queue time",
        value = intents.queueTimeAvg.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    gauge(
        namespace = namespace,
        name = "intents_in_flight_max",
        description = "Max concurrent intents",
        value = intents.inFlightMax.toDouble(),
        attributes = attributes,
        timestamp = timestamp
    ),
    gauge(
        namespace = namespace,
        name = "intents_inter_arrival_seconds_avg",
        description = "Average intent inter-arrival",
        value = intents.interArrivalAvg.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    gauge(
        namespace = namespace,
        name = "intents_inter_arrival_seconds_median",
        description = "Median intent inter-arrival",
        value = intents.interArrivalMedian.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    gauge(
        namespace = namespace,
        name = "intents_burst_max",
        description = "Max intent burst per second",
        value = intents.burstMax.toDouble(),
        attributes = attributes,
        timestamp = timestamp
    ),
    gauge(
        namespace = namespace,
        name = "intents_buffer_max_occupancy",
        description = "Peak intent buffer occupancy",
        value = intents.bufferMaxOccupancy.toDouble(),
        attributes = attributes,
        timestamp = timestamp
    ),
    counter(
        namespace = namespace,
        name = "intents_buffer_overflows_total",
        description = "Intent buffer overflows",
        value = intents.bufferOverflows,
        attributes = attributes,
        timestamp = timestamp,
        temporality = temporality
    ),
    gauge(
        namespace = namespace,
        name = "intents_plugin_overhead_seconds_avg",
        description = "Average intent plugin overhead",
        value = intents.pluginOverheadAvg.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    gauge(
        namespace = namespace,
        name = "intents_plugin_overhead_seconds_median",
        description = "Median intent plugin overhead",
        value = intents.pluginOverheadMedian.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
)

@Suppress("LongMethod")
private fun actionMetrics(
    actions: ActionMetrics,
    namespace: String,
    attributes: List<OtlpKeyValue>,
    timestamp: Long,
    temporality: AggregationTemporality,
): List<OtlpMetric> = listOf(
    counter(
        namespace = namespace,
        name = "actions_sent_total",
        description = "Actions sent",
        value = actions.sent,
        attributes = attributes,
        timestamp = timestamp,
        temporality = temporality
    ),
    counter(
        namespace = namespace,
        name = "actions_delivered_total",
        description = "Actions delivered",
        value = actions.delivered,
        attributes = attributes,
        timestamp = timestamp,
        temporality = temporality
    ),
    counter(
        namespace = namespace,
        name = "actions_undelivered_total",
        description = "Actions undelivered",
        value = actions.undelivered,
        attributes = attributes,
        timestamp = timestamp,
        temporality = temporality
    ),
    gauge(
        namespace = namespace,
        name = "actions_ops_per_second",
        description = "Action throughput",
        value = actions.opsPerSecond,
        attributes = attributes,
        timestamp = timestamp
    ),
    gauge(
        namespace = namespace,
        name = "actions_delivery_seconds_avg",
        description = "Average action delivery",
        value = actions.deliveryAvg.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    quantileGauge(
        namespace = namespace,
        name = "actions_delivery_seconds",
        description = "Action delivery quantiles",
        quantiles = ACTION_QUANTILES,
        source = actions,
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    gauge(
        namespace = namespace,
        name = "actions_queue_time_seconds_avg",
        description = "Average action queue time",
        value = actions.queueTimeAvg.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    gauge(
        namespace = namespace,
        name = "actions_queue_time_seconds_median",
        description = "Median action queue time",
        value = actions.queueTimeMedian.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    gauge(
        namespace = namespace,
        name = "actions_buffer_max_occupancy",
        description = "Peak action buffer occupancy",
        value = actions.bufferMaxOccupancy.toDouble(),
        attributes = attributes,
        timestamp = timestamp
    ),
    counter(
        namespace = namespace,
        name = "actions_buffer_overflows_total",
        description = "Action buffer overflows",
        value = actions.bufferOverflows,
        attributes = attributes,
        timestamp = timestamp,
        temporality = temporality
    ),
    gauge(
        namespace = namespace,
        name = "actions_plugin_overhead_seconds_avg",
        description = "Average action plugin overhead",
        value = actions.pluginOverheadAvg.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    gauge(
        namespace = namespace,
        name = "actions_plugin_overhead_seconds_median",
        description = "Median action plugin overhead",
        value = actions.pluginOverheadMedian.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
)

private fun stateMetrics(
    state: StateMetrics,
    namespace: String,
    attributes: List<OtlpKeyValue>,
    timestamp: Long,
    temporality: AggregationTemporality,
): List<OtlpMetric> = listOf(
    counter(
        namespace = namespace,
        name = "state_transitions_total",
        description = "State transitions",
        value = state.transitions,
        attributes = attributes,
        timestamp = timestamp,
        temporality = temporality
    ),
    counter(
        namespace = namespace,
        name = "state_transitions_vetoed_total",
        description = "Vetoed state transitions",
        value = state.transitionsVetoed,
        attributes = attributes,
        timestamp = timestamp,
        temporality = temporality
    ),
    gauge(
        namespace = namespace,
        name = "state_update_seconds_avg",
        description = "Average state update",
        value = state.updateAvg.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    quantileGauge(
        namespace = namespace,
        name = "state_update_seconds",
        description = "State update quantiles",
        quantiles = STATE_QUANTILES,
        source = state,
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    gauge(
        namespace = namespace,
        name = "state_ops_per_second",
        description = "State transition throughput",
        value = state.opsPerSecond,
        attributes = attributes,
        timestamp = timestamp
    ),
)

private fun subscriptionMetrics(
    subscriptions: SubscriptionMetrics,
    namespace: String,
    attributes: List<OtlpKeyValue>,
    timestamp: Long,
    temporality: AggregationTemporality,
): List<OtlpMetric> = listOf(
    counter(
        namespace = namespace,
        name = "subscriptions_events_total",
        description = "Subscription events",
        value = subscriptions.events,
        attributes = attributes,
        timestamp = timestamp,
        temporality = temporality
    ),
    gauge(
        namespace = namespace,
        name = "subscriptions_current",
        description = "Current subscribers",
        value = subscriptions.current.toDouble(),
        attributes = attributes,
        timestamp = timestamp
    ),
    gauge(
        namespace = namespace,
        name = "subscriptions_peak",
        description = "Peak subscribers",
        value = subscriptions.peak.toDouble(),
        attributes = attributes,
        timestamp = timestamp
    ),
    gauge(
        namespace = namespace,
        name = "subscriptions_lifetime_seconds_avg",
        description = "Average subscriber lifetime",
        value = subscriptions.lifetimeAvg.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    gauge(
        namespace = namespace,
        name = "subscriptions_lifetime_seconds_median",
        description = "Median subscriber lifetime",
        value = subscriptions.lifetimeMedian.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    gauge(
        namespace = namespace,
        name = "subscriptions_avg",
        description = "Average subscribers",
        value = subscriptions.subscribersAvg,
        attributes = attributes,
        timestamp = timestamp
    ),
    gauge(
        namespace = namespace,
        name = "subscriptions_median",
        description = "Median subscribers",
        value = subscriptions.subscribersMedian,
        attributes = attributes,
        timestamp = timestamp
    ),
)

private fun lifecycleMetrics(
    lifecycle: LifecycleMetrics,
    namespace: String,
    attributes: List<OtlpKeyValue>,
    timestamp: Long,
    temporality: AggregationTemporality,
): List<OtlpMetric> = listOf(
    counter(
        namespace = namespace,
        name = "lifecycle_start_total",
        description = "Store starts",
        value = lifecycle.startCount,
        attributes = attributes,
        timestamp = timestamp,
        temporality = temporality
    ),
    counter(
        namespace = namespace,
        name = "lifecycle_stop_total",
        description = "Store stops",
        value = lifecycle.stopCount,
        attributes = attributes,
        timestamp = timestamp,
        temporality = temporality
    ),
    gauge(
        namespace = namespace,
        name = "lifecycle_uptime_seconds_total",
        description = "Total uptime seconds",
        value = lifecycle.uptimeTotal.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    gauge(
        namespace = namespace,
        name = "lifecycle_lifetime_seconds_current",
        description = "Current lifetime seconds",
        value = lifecycle.lifetimeCurrent.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    gauge(
        namespace = namespace,
        name = "lifecycle_lifetime_seconds_avg",
        description = "Average lifetime seconds",
        value = lifecycle.lifetimeAvg.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    gauge(
        namespace = namespace,
        name = "lifecycle_lifetime_seconds_median",
        description = "Median lifetime seconds",
        value = lifecycle.lifetimeMedian.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    gauge(
        namespace = namespace,
        name = "lifecycle_bootstrap_seconds_avg",
        description = "Average bootstrap",
        value = lifecycle.bootstrapAvg.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    gauge(
        namespace = namespace,
        name = "lifecycle_bootstrap_seconds_median",
        description = "Median bootstrap",
        value = lifecycle.bootstrapMedian.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
)

private fun exceptionMetrics(
    exceptions: ExceptionMetrics,
    namespace: String,
    attributes: List<OtlpKeyValue>,
    timestamp: Long,
    temporality: AggregationTemporality,
): List<OtlpMetric> = listOf(
    counter(
        namespace = namespace,
        name = "exceptions_total",
        description = "Exceptions observed",
        value = exceptions.total,
        attributes = attributes,
        timestamp = timestamp,
        temporality = temporality
    ),
    counter(
        namespace = namespace,
        name = "exceptions_handled_total",
        description = "Exceptions handled",
        value = exceptions.handled,
        attributes = attributes,
        timestamp = timestamp,
        temporality = temporality
    ),
    gauge(
        namespace = namespace,
        name = "exceptions_recovery_seconds_avg",
        description = "Average recovery latency",
        value = exceptions.recoveryLatencyAvg.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
    gauge(
        namespace = namespace,
        name = "exceptions_recovery_seconds_median",
        description = "Median recovery latency",
        value = exceptions.recoveryLatencyMedian.seconds(),
        attributes = attributes,
        timestamp = timestamp,
        unit = SECONDS_UNIT
    ),
)

private fun counter(
    namespace: String,
    name: String,
    description: String,
    value: Long,
    attributes: List<OtlpKeyValue>,
    timestamp: Long,
    temporality: AggregationTemporality = AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE,
): OtlpMetric = OtlpMetric(
    name = metricName(namespace, name),
    description = description,
    sum = OtlpSum(
        dataPoints = listOf(
            OtlpNumberDataPoint(
                attributes = attributes,
                startTimeUnixNano = timestamp,
                timeUnixNano = timestamp,
                asInt = value
            )
        ),
        aggregationTemporality = temporality,
        isMonotonic = true
    )
)

private fun gauge(
    namespace: String,
    name: String,
    description: String,
    value: Double,
    attributes: List<OtlpKeyValue>,
    timestamp: Long,
    unit: String? = null,
): OtlpMetric = OtlpMetric(
    name = metricName(namespace, name),
    description = description,
    unit = unit,
    gauge = OtlpGauge(
        dataPoints = listOf(
            OtlpNumberDataPoint(
                attributes = attributes,
                startTimeUnixNano = timestamp,
                timeUnixNano = timestamp,
                asDouble = value
            )
        )
    )
)

private fun <T> quantileGauge(
    namespace: String,
    name: String,
    description: String,
    quantiles: List<QuantileSpec<T>>,
    source: T,
    attributes: List<OtlpKeyValue>,
    timestamp: Long,
    unit: String? = null,
): OtlpMetric = OtlpMetric(
    name = metricName(namespace, name),
    description = description,
    unit = unit,
    gauge = OtlpGauge(
        dataPoints = quantiles.map { spec ->
            OtlpNumberDataPoint(
                attributes = attributes.withAttribute("quantile", spec.quantile.label),
                startTimeUnixNano = timestamp,
                timeUnixNano = timestamp,
                asDouble = spec.accessor(source).seconds()
            )
        }
    )
)

private fun metricName(namespace: String, name: String): String = "${namespace}_$name"

private fun baseAttributes(meta: Meta, extras: Map<String, String>): List<OtlpKeyValue> {
    val merged = linkedMapOf<String, String>()
    merged.putAll(extras)
    meta.storeName?.let { if (!merged.containsKey("store")) merged["store"] = it }
    meta.storeId?.let { if (!merged.containsKey("store_id")) merged["store_id"] = it }
    return merged.entries
        .sortedBy { it.key }
        .map { (key, value) -> OtlpKeyValue(key, OtlpAnyValue(stringValue = value)) }
}

private fun List<OtlpKeyValue>.withAttribute(key: String, value: String): List<OtlpKeyValue> =
    this + OtlpKeyValue(key, OtlpAnyValue(stringValue = value))

private fun Instant.toEpochNanoseconds(): Long = toEpochMilliseconds() * NANOS_PER_MILLI

private fun Duration.seconds(): Double = when {
    isInfinite() -> Double.POSITIVE_INFINITY
    else -> inWholeNanoseconds / NANOS_IN_SECOND
}
