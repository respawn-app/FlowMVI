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
private const val NANOS_IN_SECOND_LONG: Long = 1_000_000_000
private const val NANOS_IN_SECOND: Double = 1_000_000_000.0

private val DefaultJson: Json = Json {
    encodeDefaults = false
    allowSpecialFloatingPointValues = true
}

/** Maps a [MetricsSnapshot] to an OTLP JSON payload ready for serialization. */
public fun MetricsSnapshot.toOtlpPayload(
    namespace: String = DEFAULT_NAMESPACE,
    resourceAttributes: Map<String, String> = emptyMap(),
    resourceAttributesProvider: (Meta) -> Map<String, String> = { resourceAttributes },
    scopeName: String = DEFAULT_SCOPE,
    temporality: AggregationTemporality = AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE,
): OtlpMetricsPayload {
    val timestamp = meta.generatedAt.toEpochNanoseconds()
    val startTime = meta.startTime?.toEpochNanoseconds()
        ?: (timestamp - meta.windowSeconds.toLong() * NANOS_IN_SECOND_LONG).coerceAtLeast(0)
    val attributes = baseAttributes(meta, resourceAttributesProvider(meta))
    val context = MetricContext(namespace, attributes, timestamp, startTime, temporality)
    return OtlpMetricsPayload(
        resourceMetrics = listOf(
            OtlpResourceMetrics(
                resource = OtlpResource(attributes),
                scopeMetrics = listOf(
                    OtlpScopeMetrics(
                        scope = OtlpInstrumentationScope(name = scopeName),
                        metrics = metrics(snapshot = this, context = context)
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
    resourceAttributesProvider: (Meta) -> Map<String, String> = { resourceAttributes },
    temporality: AggregationTemporality = AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE,
    json: Json = DefaultJson,
): MetricsSink = MappingSink(delegate) { snapshot ->
    val payload = snapshot.toOtlpPayload(
        namespace = namespace,
        resourceAttributes = resourceAttributes,
        resourceAttributesProvider = resourceAttributesProvider,
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

private data class MetricContext(
    val namespace: String,
    val attributes: List<OtlpKeyValue>,
    val timestamp: Long,
    val startTime: Long,
    val temporality: AggregationTemporality,
)

private fun metrics(
    snapshot: MetricsSnapshot,
    context: MetricContext,
): List<OtlpMetric> = buildList {
    addAll(configMetrics(snapshot, context))
    addAll(intentMetrics(snapshot.intents, context))
    addAll(actionMetrics(snapshot.actions, context))
    addAll(stateMetrics(snapshot.state, context))
    addAll(subscriptionMetrics(snapshot.subscriptions, context))
    addAll(lifecycleMetrics(snapshot.lifecycle, context))
    addAll(exceptionMetrics(snapshot.exceptions, context))
}

private fun configMetrics(
    snapshot: MetricsSnapshot,
    context: MetricContext,
): List<OtlpMetric> = listOf(
    context.gauge(
        name = "config_window_seconds",
        description = "Metrics sampling window length",
        value = snapshot.meta.windowSeconds.toDouble(),
    ),
    context.gauge(
        name = "config_ema_alpha",
        description = "Metrics EMA smoothing factor",
        value = snapshot.meta.emaAlpha.toDouble(),
    ),
)

@Suppress("LongMethod")
private fun intentMetrics(
    intents: IntentMetrics,
    context: MetricContext,
): List<OtlpMetric> = listOf(
    context.counter(
        name = "intents_total",
        description = "Total intents",
        value = intents.total,
    ),
    context.counter(
        name = "intents_processed_total",
        description = "Processed intents",
        value = intents.processed,
    ),
    context.counter(
        name = "intents_dropped_total",
        description = "Dropped intents",
        value = intents.dropped,
    ),
    context.counter(
        name = "intents_undelivered_total",
        description = "Undelivered intents",
        value = intents.undelivered,
    ),
    context.gauge(
        name = "intents_ops_per_second",
        description = "Intent throughput",
        value = intents.opsPerSecond,
    ),
    context.gauge(
        name = "intents_duration_seconds_avg",
        description = "Average intent duration",
        value = intents.durationAvg.seconds(),
        unit = SECONDS_UNIT
    ),
    context.quantileGauge(
        name = "intents_duration_seconds",
        description = "Intent duration quantiles",
        quantiles = INTENT_QUANTILES,
        source = intents,
        unit = SECONDS_UNIT
    ),
    context.gauge(
        name = "intents_queue_time_seconds_avg",
        description = "Average intent queue time",
        value = intents.queueTimeAvg.seconds(),
        unit = SECONDS_UNIT
    ),
    context.gauge(
        name = "intents_in_flight_max",
        description = "Max concurrent intents",
        value = intents.inFlightMax.toDouble(),
    ),
    context.gauge(
        name = "intents_inter_arrival_seconds_avg",
        description = "Average intent inter-arrival",
        value = intents.interArrivalAvg.seconds(),
        unit = SECONDS_UNIT
    ),
    context.gauge(
        name = "intents_inter_arrival_seconds_median",
        description = "Median intent inter-arrival",
        value = intents.interArrivalMedian.seconds(),
        unit = SECONDS_UNIT
    ),
    context.gauge(
        name = "intents_burst_max",
        description = "Max intent burst per second",
        value = intents.burstMax.toDouble(),
    ),
    context.gauge(
        name = "intents_buffer_max_occupancy",
        description = "Peak intent buffer occupancy",
        value = intents.bufferMaxOccupancy.toDouble(),
    ),
    context.counter(
        name = "intents_buffer_overflows_total",
        description = "Intent buffer overflows",
        value = intents.bufferOverflows,
    ),
    context.gauge(
        name = "intents_plugin_overhead_seconds_avg",
        description = "Average intent plugin overhead",
        value = intents.pluginOverheadAvg.seconds(),
        unit = SECONDS_UNIT
    ),
    context.gauge(
        name = "intents_plugin_overhead_seconds_median",
        description = "Median intent plugin overhead",
        value = intents.pluginOverheadMedian.seconds(),
        unit = SECONDS_UNIT
    ),
)

@Suppress("LongMethod")
private fun actionMetrics(
    actions: ActionMetrics,
    context: MetricContext,
): List<OtlpMetric> = listOf(
    context.counter(
        name = "actions_sent_total",
        description = "Actions sent",
        value = actions.sent,
    ),
    context.counter(
        name = "actions_delivered_total",
        description = "Actions delivered",
        value = actions.delivered,
    ),
    context.counter(
        name = "actions_undelivered_total",
        description = "Actions undelivered",
        value = actions.undelivered,
    ),
    context.gauge(
        name = "actions_ops_per_second",
        description = "Action throughput",
        value = actions.opsPerSecond,
    ),
    context.gauge(
        name = "actions_delivery_seconds_avg",
        description = "Average action delivery",
        value = actions.deliveryAvg.seconds(),
        unit = SECONDS_UNIT
    ),
    context.quantileGauge(
        name = "actions_delivery_seconds",
        description = "Action delivery quantiles",
        quantiles = ACTION_QUANTILES,
        source = actions,
        unit = SECONDS_UNIT
    ),
    context.gauge(
        name = "actions_queue_time_seconds_avg",
        description = "Average action queue time",
        value = actions.queueTimeAvg.seconds(),
        unit = SECONDS_UNIT
    ),
    context.gauge(
        name = "actions_queue_time_seconds_median",
        description = "Median action queue time",
        value = actions.queueTimeMedian.seconds(),
        unit = SECONDS_UNIT
    ),
    context.gauge(
        name = "actions_buffer_max_occupancy",
        description = "Peak action buffer occupancy",
        value = actions.bufferMaxOccupancy.toDouble(),
    ),
    context.counter(
        name = "actions_buffer_overflows_total",
        description = "Action buffer overflows",
        value = actions.bufferOverflows,
    ),
    context.gauge(
        name = "actions_plugin_overhead_seconds_avg",
        description = "Average action plugin overhead",
        value = actions.pluginOverheadAvg.seconds(),
        unit = SECONDS_UNIT
    ),
    context.gauge(
        name = "actions_plugin_overhead_seconds_median",
        description = "Median action plugin overhead",
        value = actions.pluginOverheadMedian.seconds(),
        unit = SECONDS_UNIT
    ),
)

private fun stateMetrics(
    state: StateMetrics,
    context: MetricContext,
): List<OtlpMetric> = listOf(
    context.counter(
        name = "state_transitions_total",
        description = "State transitions",
        value = state.transitions,
    ),
    context.counter(
        name = "state_transitions_vetoed_total",
        description = "Vetoed state transitions",
        value = state.transitionsVetoed,
    ),
    context.gauge(
        name = "state_update_seconds_avg",
        description = "Average state update",
        value = state.updateAvg.seconds(),
        unit = SECONDS_UNIT
    ),
    context.quantileGauge(
        name = "state_update_seconds",
        description = "State update quantiles",
        quantiles = STATE_QUANTILES,
        source = state,
        unit = SECONDS_UNIT
    ),
    context.gauge(
        name = "state_ops_per_second",
        description = "State transition throughput",
        value = state.opsPerSecond,
    ),
)

private fun subscriptionMetrics(
    subscriptions: SubscriptionMetrics,
    context: MetricContext,
): List<OtlpMetric> = listOf(
    context.counter(
        name = "subscriptions_events_total",
        description = "Subscription events",
        value = subscriptions.events,
    ),
    context.gauge(
        name = "subscriptions_current",
        description = "Current subscribers",
        value = subscriptions.current.toDouble(),
    ),
    context.gauge(
        name = "subscriptions_peak",
        description = "Peak subscribers",
        value = subscriptions.peak.toDouble(),
    ),
    context.gauge(
        name = "subscriptions_lifetime_seconds_avg",
        description = "Average subscriber lifetime",
        value = subscriptions.lifetimeAvg.seconds(),
        unit = SECONDS_UNIT
    ),
    context.gauge(
        name = "subscriptions_lifetime_seconds_median",
        description = "Median subscriber lifetime",
        value = subscriptions.lifetimeMedian.seconds(),
        unit = SECONDS_UNIT
    ),
    context.gauge(
        name = "subscriptions_avg",
        description = "Average subscribers",
        value = subscriptions.subscribersAvg,
    ),
    context.gauge(
        name = "subscriptions_median",
        description = "Median subscribers",
        value = subscriptions.subscribersMedian,
    ),
)

private fun lifecycleMetrics(
    lifecycle: LifecycleMetrics,
    context: MetricContext,
): List<OtlpMetric> = listOf(
    context.counter(
        name = "lifecycle_start_total",
        description = "Store starts",
        value = lifecycle.startCount,
    ),
    context.counter(
        name = "lifecycle_stop_total",
        description = "Store stops",
        value = lifecycle.stopCount,
    ),
    context.gauge(
        name = "lifecycle_uptime_seconds_total",
        description = "Total uptime seconds",
        value = lifecycle.uptimeTotal.seconds(),
        unit = SECONDS_UNIT
    ),
    context.gauge(
        name = "lifecycle_lifetime_seconds_current",
        description = "Current lifetime seconds",
        value = lifecycle.lifetimeCurrent.seconds(),
        unit = SECONDS_UNIT
    ),
    context.gauge(
        name = "lifecycle_lifetime_seconds_avg",
        description = "Average lifetime seconds",
        value = lifecycle.lifetimeAvg.seconds(),
        unit = SECONDS_UNIT
    ),
    context.gauge(
        name = "lifecycle_lifetime_seconds_median",
        description = "Median lifetime seconds",
        value = lifecycle.lifetimeMedian.seconds(),
        unit = SECONDS_UNIT
    ),
    context.gauge(
        name = "lifecycle_bootstrap_seconds_avg",
        description = "Average bootstrap",
        value = lifecycle.bootstrapAvg.seconds(),
        unit = SECONDS_UNIT
    ),
    context.gauge(
        name = "lifecycle_bootstrap_seconds_median",
        description = "Median bootstrap",
        value = lifecycle.bootstrapMedian.seconds(),
        unit = SECONDS_UNIT
    ),
)

private fun exceptionMetrics(
    exceptions: ExceptionMetrics,
    context: MetricContext,
): List<OtlpMetric> = listOf(
    context.counter(
        name = "exceptions_total",
        description = "Exceptions observed",
        value = exceptions.total,
    ),
    context.counter(
        name = "exceptions_handled_total",
        description = "Exceptions handled",
        value = exceptions.handled,
    ),
    context.gauge(
        name = "exceptions_recovery_seconds_avg",
        description = "Average recovery latency",
        value = exceptions.recoveryLatencyAvg.seconds(),
        unit = SECONDS_UNIT
    ),
    context.gauge(
        name = "exceptions_recovery_seconds_median",
        description = "Median recovery latency",
        value = exceptions.recoveryLatencyMedian.seconds(),
        unit = SECONDS_UNIT
    ),
)

private fun MetricContext.counter(
    name: String,
    description: String,
    value: Long,
): OtlpMetric = OtlpMetric(
    name = metricName(namespace, name),
    description = description,
    sum = OtlpSum(
        dataPoints = listOf(
            OtlpNumberDataPoint(
                attributes = attributes,
                startTimeUnixNano = startTime,
                timeUnixNano = timestamp,
                asInt = value
            )
        ),
        aggregationTemporality = temporality,
        isMonotonic = true
    )
)

private fun MetricContext.gauge(
    name: String,
    description: String,
    value: Double,
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

private fun <T> MetricContext.quantileGauge(
    name: String,
    description: String,
    quantiles: List<QuantileSpec<T>>,
    source: T,
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
