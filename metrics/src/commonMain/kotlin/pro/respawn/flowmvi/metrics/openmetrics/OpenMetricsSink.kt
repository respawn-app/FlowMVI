package pro.respawn.flowmvi.metrics.openmetrics

import pro.respawn.flowmvi.metrics.MappingSink
import pro.respawn.flowmvi.metrics.MetricsSink
import pro.respawn.flowmvi.metrics.api.ActionMetrics
import pro.respawn.flowmvi.metrics.api.IntentMetrics
import pro.respawn.flowmvi.metrics.api.MetricsSnapshot
import pro.respawn.flowmvi.metrics.api.Sink
import pro.respawn.flowmvi.metrics.api.StateMetrics
import kotlin.time.Duration

private enum class MetricType(val wire: String) {
    Counter("counter"),
    Gauge("gauge")
}

private data class Sample(
    val value: Double,
    val labels: Map<String, String> = emptyMap(),
    val timestampMillis: Long? = null,
)

private data class Metric(
    val name: String,
    val help: String?,
    val unit: String?,
    val type: MetricType,
    val samples: List<Sample>,
)

private class OpenMetricsRenderer(
    private val namespace: String,
    private val includeHelp: Boolean,
    private val includeUnit: Boolean,
    private val includeTimestamp: Boolean,
    private val trailingEof: Boolean,
) {

    fun render(snapshot: MetricsSnapshot): String {
        val builder = StringBuilder()
        val base = baseLabels(snapshot)
        val timestamp = snapshot.meta.generatedAt.toEpochMilliseconds().takeIf { includeTimestamp }
        metrics(snapshot, base, timestamp).forEach { metric -> appendMetric(builder, metric) }
        if (trailingEof) builder.append(EOF_LINE)
        return builder.toString()
    }

    private fun appendMetric(builder: StringBuilder, metric: Metric) {
        val fullName = "${namespace}_${metric.name}"
        if (includeHelp && metric.help != null) {
            builder.append(HELP_PREFIX).append(fullName).append(' ')
                .append(metric.help.replace('\n', ' ')).append('\n')
        }
        if (includeUnit && metric.unit != null) {
            builder.append(UNIT_PREFIX).append(fullName).append(' ').append(metric.unit).append('\n')
        }
        builder.append(TYPE_PREFIX).append(fullName).append(' ').append(metric.type.wire).append('\n')
        metric.samples.forEach { sample ->
            builder.append(fullName)
            if (sample.labels.isNotEmpty()) builder.append('{').append(labels(sample.labels)).append('}')
            builder.append(' ').append(formatValue(sample.value))
            if (includeTimestamp && sample.timestampMillis != null) {
                builder.append(' ').append(sample.timestampMillis)
            }
            builder.append('\n')
        }
    }

    private fun labels(labels: Map<String, String>): String =
        labels.toList().sortedBy { it.first }.joinToString(",") { (k, v) ->
            val escaped = v.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"")
            "$k=\"$escaped\""
        }

    private fun formatValue(value: Double): String = when {
        value.isNaN() -> "NaN"
        value == Double.POSITIVE_INFINITY -> "+Inf"
        value == Double.NEGATIVE_INFINITY -> "-Inf"
        else -> value.toString()
    }
}

private const val EOF_LINE: String = "# EOF\n"
private const val HELP_PREFIX: String = "# HELP "
private const val UNIT_PREFIX: String = "# UNIT "
private const val TYPE_PREFIX: String = "# TYPE "
private const val SECONDS_UNIT: String = "seconds"

private data class QuantileSpec<T>(val label: String, val accessor: (T) -> Duration)

private val QUANTILES: List<QuantileSpec<IntentMetrics>> = listOf(
    QuantileSpec("0.5") { it.durationP50 },
    QuantileSpec("0.9") { it.durationP90 },
    QuantileSpec("0.95") { it.durationP95 },
    QuantileSpec("0.99") { it.durationP99 },
)
private val ACTION_QUANTILES: List<QuantileSpec<ActionMetrics>> = listOf(
    QuantileSpec("0.5") { it.deliveryP50 },
    QuantileSpec("0.9") { it.deliveryP90 },
    QuantileSpec("0.95") { it.deliveryP95 },
    QuantileSpec("0.99") { it.deliveryP99 },
)
private val STATE_QUANTILES: List<QuantileSpec<StateMetrics>> = listOf(
    QuantileSpec("0.5") { it.updateP50 },
    QuantileSpec("0.9") { it.updateP90 },
    QuantileSpec("0.95") { it.updateP95 },
    QuantileSpec("0.99") { it.updateP99 },
)

private fun metrics(snapshot: MetricsSnapshot, base: Map<String, String>, timestampMillis: Long?): List<Metric> =
    buildList {
        addAll(intentMetrics(snapshot, base, timestampMillis))
        addAll(actionMetrics(snapshot, base, timestampMillis))
        addAll(stateMetrics(snapshot, base, timestampMillis))
        addAll(subscriptionMetrics(snapshot, base, timestampMillis))
        addAll(lifecycleMetrics(snapshot, base, timestampMillis))
        addAll(exceptionMetrics(snapshot, base, timestampMillis))
    }

private fun intentMetrics(snapshot: MetricsSnapshot, base: Map<String, String>, timestampMillis: Long?): List<Metric> {
    val intents = snapshot.intents
    return listOf(
        counter(
            name = "intents_total",
            help = "Total intents",
            value = intents.total,
            base = base,
            timestampMillis = timestampMillis
        ),
        counter(
            name = "intents_processed_total",
            help = "Processed intents",
            value = intents.processed,
            base = base,
            timestampMillis = timestampMillis
        ),
        counter(
            name = "intents_dropped_total",
            help = "Dropped intents",
            value = intents.dropped,
            base = base,
            timestampMillis = timestampMillis
        ),
        counter(
            name = "intents_undelivered_total",
            help = "Undelivered intents",
            value = intents.undelivered,
            base = base,
            timestampMillis = timestampMillis
        ),
        gauge(
            name = "intents_ops_per_second",
            help = "Intent throughput",
            value = intents.opsPerSecond,
            base = base,
            timestampMillis = timestampMillis
        ),
        gauge(
            name = "intents_duration_seconds_avg",
            help = "Average intent duration",
            value = intents.durationAvg.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        quantileGauge(
            name = "intents_duration_seconds",
            help = "Intent duration quantiles",
            quantiles = QUANTILES,
            source = intents,
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        gauge(
            name = "intents_queue_time_seconds_avg",
            help = "Average intent queue time",
            value = intents.queueTimeAvg.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        gauge(
            name = "intents_in_flight_max",
            help = "Max concurrent intents",
            value = intents.inFlightMax.toDouble(),
            base = base,
            timestampMillis = timestampMillis
        ),
        gauge(
            name = "intents_inter_arrival_seconds_avg",
            help = "Average intent inter-arrival",
            value = intents.interArrivalAvg.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        gauge(
            name = "intents_inter_arrival_seconds_median",
            help = "Median intent inter-arrival",
            value = intents.interArrivalMedian.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        gauge(
            name = "intents_burst_max",
            help = "Max intent burst per second",
            value = intents.burstMax.toDouble(),
            base = base,
            timestampMillis = timestampMillis
        ),
        gauge(
            name = "intents_buffer_max_occupancy",
            help = "Peak intent buffer occupancy",
            value = intents.bufferMaxOccupancy.toDouble(),
            base = base,
            timestampMillis = timestampMillis
        ),
        counter(
            name = "intents_buffer_overflows_total",
            help = "Intent buffer overflows",
            value = intents.bufferOverflows,
            base = base,
            timestampMillis = timestampMillis
        ),
        gauge(
            name = "intents_plugin_overhead_seconds_avg",
            help = "Average intent plugin overhead",
            value = intents.pluginOverheadAvg.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        gauge(
            name = "intents_plugin_overhead_seconds_median",
            help = "Median intent plugin overhead",
            value = intents.pluginOverheadMedian.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
    )
}

private fun actionMetrics(snapshot: MetricsSnapshot, base: Map<String, String>, timestampMillis: Long?): List<Metric> {
    val actions = snapshot.actions
    return listOf(
        counter(
            name = "actions_sent_total",
            help = "Actions sent",
            value = actions.sent,
            base = base,
            timestampMillis = timestampMillis
        ),
        counter(
            name = "actions_delivered_total",
            help = "Actions delivered",
            value = actions.delivered,
            base = base,
            timestampMillis = timestampMillis
        ),
        counter(
            name = "actions_undelivered_total",
            help = "Actions undelivered",
            value = actions.undelivered,
            base = base,
            timestampMillis = timestampMillis
        ),
        gauge(
            name = "actions_ops_per_second",
            help = "Action throughput",
            value = actions.opsPerSecond,
            base = base,
            timestampMillis = timestampMillis
        ),
        gauge(
            name = "actions_delivery_seconds_avg",
            help = "Average action delivery",
            value = actions.deliveryAvg.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        quantileGauge(
            name = "actions_delivery_seconds",
            help = "Action delivery quantiles",
            quantiles = ACTION_QUANTILES,
            source = actions,
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        gauge(
            name = "actions_queue_time_seconds_avg",
            help = "Average action queue time",
            value = actions.queueTimeAvg.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        gauge(
            name = "actions_queue_time_seconds_median",
            help = "Median action queue time",
            value = actions.queueTimeMedian.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        gauge(
            name = "actions_buffer_max_occupancy",
            help = "Peak action buffer occupancy",
            value = actions.bufferMaxOccupancy.toDouble(),
            base = base,
            timestampMillis = timestampMillis
        ),
        counter(
            name = "actions_buffer_overflows_total",
            help = "Action buffer overflows",
            value = actions.bufferOverflows,
            base = base,
            timestampMillis = timestampMillis
        ),
        gauge(
            name = "actions_plugin_overhead_seconds_avg",
            help = "Average action plugin overhead",
            value = actions.pluginOverheadAvg.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        gauge(
            name = "actions_plugin_overhead_seconds_median",
            help = "Median action plugin overhead",
            value = actions.pluginOverheadMedian.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
    )
}

private fun stateMetrics(snapshot: MetricsSnapshot, base: Map<String, String>, timestampMillis: Long?): List<Metric> {
    val state = snapshot.state
    return listOf(
        counter(
            name = "state_transitions_total",
            help = "State transitions",
            value = state.transitions,
            base = base,
            timestampMillis = timestampMillis
        ),
        counter(
            name = "state_transitions_vetoed_total",
            help = "Vetoed state transitions",
            value = state.transitionsVetoed,
            base = base,
            timestampMillis = timestampMillis
        ),
        gauge(
            name = "state_update_seconds_avg",
            help = "Average state update",
            value = state.updateAvg.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        quantileGauge(
            name = "state_update_seconds",
            help = "State update quantiles",
            quantiles = STATE_QUANTILES,
            source = state,
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        gauge(
            name = "state_ops_per_second",
            help = "State transition throughput",
            value = state.opsPerSecond,
            base = base,
            timestampMillis = timestampMillis
        ),
    )
}

private fun subscriptionMetrics(snapshot: MetricsSnapshot, base: Map<String, String>, timestampMillis: Long?): List<Metric> {
    val subs = snapshot.subscriptions
    return listOf(
        counter(
            name = "subscriptions_events_total",
            help = "Subscription events",
            value = subs.events,
            base = base,
            timestampMillis = timestampMillis
        ),
        gauge(
            name = "subscriptions_current",
            help = "Current subscribers",
            value = subs.current.toDouble(),
            base = base,
            timestampMillis = timestampMillis
        ),
        gauge(
            name = "subscriptions_peak",
            help = "Peak subscribers",
            value = subs.peak.toDouble(),
            base = base,
            timestampMillis = timestampMillis
        ),
        gauge(
            name = "subscriptions_lifetime_seconds_avg",
            help = "Average subscriber lifetime",
            value = subs.lifetimeAvg.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        gauge(
            name = "subscriptions_lifetime_seconds_median",
            help = "Median subscriber lifetime",
            value = subs.lifetimeMedian.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        gauge(
            name = "subscriptions_avg",
            help = "Average subscribers",
            value = subs.subscribersAvg,
            base = base,
            timestampMillis = timestampMillis
        ),
        gauge(
            name = "subscriptions_median",
            help = "Median subscribers",
            value = subs.subscribersMedian,
            base = base,
            timestampMillis = timestampMillis
        ),
    )
}

private fun lifecycleMetrics(snapshot: MetricsSnapshot, base: Map<String, String>, timestampMillis: Long?): List<Metric> {
    val life = snapshot.lifecycle
    return listOf(
        counter(
            name = "lifecycle_start_total",
            help = "Store starts",
            value = life.startCount,
            base = base,
            timestampMillis = timestampMillis
        ),
        counter(
            name = "lifecycle_stop_total",
            help = "Store stops",
            value = life.stopCount,
            base = base,
            timestampMillis = timestampMillis
        ),
        gauge(
            name = "lifecycle_uptime_seconds_total",
            help = "Total uptime seconds",
            value = life.uptimeTotal.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        gauge(
            name = "lifecycle_lifetime_seconds_current",
            help = "Current lifetime seconds",
            value = life.lifetimeCurrent.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        gauge(
            name = "lifecycle_lifetime_seconds_avg",
            help = "Average lifetime seconds",
            value = life.lifetimeAvg.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        gauge(
            name = "lifecycle_lifetime_seconds_median",
            help = "Median lifetime seconds",
            value = life.lifetimeMedian.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        gauge(
            name = "lifecycle_bootstrap_seconds_avg",
            help = "Average bootstrap",
            value = life.bootstrapAvg.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        gauge(
            name = "lifecycle_bootstrap_seconds_median",
            help = "Median bootstrap",
            value = life.bootstrapMedian.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
    )
}

private fun exceptionMetrics(snapshot: MetricsSnapshot, base: Map<String, String>, timestampMillis: Long?): List<Metric> {
    val exceptions = snapshot.exceptions
    return listOf(
        counter(
            name = "exceptions_total",
            help = "Exceptions observed",
            value = exceptions.total,
            base = base,
            timestampMillis = timestampMillis
        ),
        counter(
            name = "exceptions_handled_total",
            help = "Exceptions handled",
            value = exceptions.handled,
            base = base,
            timestampMillis = timestampMillis
        ),
        gauge(
            name = "exceptions_recovery_seconds_avg",
            help = "Average recovery latency",
            value = exceptions.recoveryLatencyAvg.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
        gauge(
            name = "exceptions_recovery_seconds_median",
            help = "Median recovery latency",
            value = exceptions.recoveryLatencyMedian.seconds(),
            base = base,
            timestampMillis = timestampMillis,
            unit = SECONDS_UNIT
        ),
    )
}

private fun counter(name: String, help: String, value: Long, base: Map<String, String>, timestampMillis: Long?): Metric =
    Metric(
        name = name,
        help = help,
        unit = null,
        type = MetricType.Counter,
        samples = listOf(Sample(value.toDouble(), base, timestampMillis))
    )

private fun gauge(
    name: String,
    help: String,
    value: Double,
    base: Map<String, String>,
    timestampMillis: Long?,
    unit: String? = null,
): Metric = Metric(
    name = name,
    help = help,
    unit = unit,
    type = MetricType.Gauge,
    samples = listOf(Sample(value, base, timestampMillis))
)

private fun <T> quantileGauge(
    name: String,
    help: String,
    quantiles: List<QuantileSpec<T>>,
    source: T,
    base: Map<String, String>,
    timestampMillis: Long?,
    unit: String? = null,
): Metric = Metric(
    name = name,
    help = help,
    unit = unit,
    type = MetricType.Gauge,
    samples = quantiles.map { spec ->
        Sample(spec.accessor(source).seconds(), base + ("quantile" to spec.label), timestampMillis)
    },
)

private fun baseLabels(snapshot: MetricsSnapshot): Map<String, String> = buildMap {
    snapshot.meta.storeName?.let { put("store", it) }
    snapshot.meta.storeId?.let { put("store_id", it) }
    put("window_seconds", snapshot.meta.windowSeconds.toString())
    put("ema_alpha", snapshot.meta.emaAlpha.toString())
}

private fun Duration.seconds(): Double = when {
    isInfinite() -> Double.POSITIVE_INFINITY
    else -> inWholeNanoseconds / 1_000_000_000.0
}

/** Builds a sink that emits OpenMetrics-compliant text. */
public fun OpenMetricsSink(
    delegate: Sink<String>,
    namespace: String = "flowmvi",
    includeHelp: Boolean = true,
    includeUnit: Boolean = true,
    includeTimestamp: Boolean = false,
): MetricsSink = MappingSink(delegate) {
    OpenMetricsRenderer(
        namespace = namespace,
        includeHelp = includeHelp,
        includeUnit = includeUnit,
        includeTimestamp = includeTimestamp,
        trailingEof = true
    ).render(it)
}

/** Builds a sink that emits Prometheus exposition format (0.0.4) text. */
public fun PrometheusSink(
    delegate: Sink<String>,
    namespace: String = "flowmvi",
    includeHelp: Boolean = true,
    includeUnit: Boolean = false,
    includeTimestamp: Boolean = false,
): MetricsSink = MappingSink(delegate) {
    OpenMetricsRenderer(
        namespace = namespace,
        includeHelp = includeHelp,
        includeUnit = includeUnit,
        includeTimestamp = includeTimestamp,
        trailingEof = false
    ).render(it)
}
