package pro.respawn.flowmvi.metrics.otel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Root OTLP metrics payload following the OpenTelemetry proto JSON mapping.
 *
 * @property resourceMetrics batches of metrics grouped by resource.
 */
@Serializable
public data class OtlpMetricsPayload(
    val resourceMetrics: List<OtlpResourceMetrics>,
)

/**
 * Metrics attached to a single resource with their instrumentation scopes.
 *
 * @property resource attributes describing the emitting resource.
 * @property scopeMetrics metric collections grouped by instrumentation scope.
 */
@Serializable
public data class OtlpResourceMetrics(
    val resource: OtlpResource? = null,
    val scopeMetrics: List<OtlpScopeMetrics>,
)

/**
 * OTLP resource description.
 *
 * @property attributes resource-level attributes (service name, store id, etc).
 */
@Serializable
public data class OtlpResource(
    val attributes: List<OtlpKeyValue> = emptyList(),
)

/**
 * Metrics produced by a single instrumentation scope.
 *
 * @property scope scope identity for the meter/provider.
 * @property metrics concrete metric data.
 */
@Serializable
public data class OtlpScopeMetrics(
    val scope: OtlpInstrumentationScope? = null,
    val metrics: List<OtlpMetric>,
)

/**
 * Describes the instrumentation scope (library) that produced the metrics.
 *
 * @property name scope name, usually a library or component id.
 * @property version optional semantic version of the scope.
 * @property attributes optional scope attributes.
 */
@Serializable
public data class OtlpInstrumentationScope(
    val name: String,
    val version: String? = null,
    val attributes: List<OtlpKeyValue> = emptyList(),
)

/**
 * OTLP metric definition containing one data type (gauge or sum).
 *
 * @property name fully-qualified metric name.
 * @property description human readable description.
 * @property unit unit of measure in OTLP notation.
 * @property gauge gauge payload when the metric is a gauge.
 * @property sum sum payload when the metric is a counter/sum.
 */
@Serializable
public data class OtlpMetric(
    val name: String,
    val description: String? = null,
    val unit: String? = null,
    val gauge: OtlpGauge? = null,
    val sum: OtlpSum? = null,
)

/**
 * Gauge metric data points.
 *
 * @property dataPoints measurements for this gauge.
 */
@Serializable
public data class OtlpGauge(
    val dataPoints: List<OtlpNumberDataPoint>,
)

/**
 * Sum metric payload.
 *
 * @property dataPoints cumulative or delta data points.
 * @property aggregationTemporality aggregation window.
 * @property isMonotonic whether the sum is strictly increasing.
 */
@Serializable
public data class OtlpSum(
    val dataPoints: List<OtlpNumberDataPoint>,
    val aggregationTemporality: AggregationTemporality,
    val isMonotonic: Boolean,
)

/**
 * Numeric data point used by gauge and sum types.
 *
 * @property attributes dimension labels attached to this sample.
 * @property startTimeUnixNano inclusive start of the aggregation period.
 * @property timeUnixNano end timestamp of the aggregation period.
 * @property asDouble double representation of the value, if used.
 * @property asInt integer representation of the value, if used.
 */
@Serializable
public data class OtlpNumberDataPoint(
    val attributes: List<OtlpKeyValue> = emptyList(),
    val startTimeUnixNano: Long = 0,
    val timeUnixNano: Long = 0,
    val asDouble: Double? = null,
    val asInt: Long? = null,
)

/**
 * Key/value attribute as defined by OTLP.
 *
 * @property key attribute key.
 * @property value attribute value encoded as [OtlpAnyValue].
 */
@Serializable
public data class OtlpKeyValue(
    val key: String,
    val value: OtlpAnyValue,
)

/**
 * Polymorphic OTLP attribute value.
 *
 * Only the populated field should be non-null.
 *
 * @property stringValue string value, if present.
 * @property boolValue boolean value, if present.
 * @property intValue integer value, if present.
 * @property doubleValue double value, if present.
 */
@Serializable
public data class OtlpAnyValue(
    val stringValue: String? = null,
    val boolValue: Boolean? = null,
    val intValue: Long? = null,
    val doubleValue: Double? = null,
)

/**
 * Aggregation temporality for sum metrics.
 */
@Serializable
public enum class AggregationTemporality {
    /** Temporality not specified by the producer. */
    @SerialName("AGGREGATION_TEMPORALITY_UNSPECIFIED")
    AGGREGATION_TEMPORALITY_UNSPECIFIED,

    /** Delta between successive collections. */
    @SerialName("AGGREGATION_TEMPORALITY_DELTA")
    AGGREGATION_TEMPORALITY_DELTA,

    /** Cumulative value since the start time. */
    @SerialName("AGGREGATION_TEMPORALITY_CUMULATIVE")
    AGGREGATION_TEMPORALITY_CUMULATIVE,
}
