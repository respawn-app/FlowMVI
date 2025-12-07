package pro.respawn.flowmvi.metrics.otel

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldContainIgnoringCase
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import pro.respawn.flowmvi.metrics.AppendableStringSink
import pro.respawn.flowmvi.metrics.api.MetricsSchemaVersion
import pro.respawn.flowmvi.metrics.otel.AggregationTemporality
import pro.respawn.flowmvi.metrics.openmetrics.OpenMetricsSink
import pro.respawn.flowmvi.metrics.sampleSnapshot
import kotlin.time.Instant

class OtlpJsonSinkTest : FreeSpec({

    val json = Json {
        encodeDefaults = false
        allowSpecialFloatingPointValues = true
    }

    val snapshot = sampleSnapshot()

    "resource attributes include store identity and extras" {
        val payload = snapshot.toOtlpPayload(
            resourceAttributes = mapOf("service.name" to "demo-service"),
            scopeName = "flowmvi.metrics"
        )

        val attributes = payload.resourceMetrics
            .single()
            .resource!!
            .attributes
            .associate { it.key to it.value.stringValue }
        attributes shouldBe mapOf(
            "schema.version" to MetricsSchemaVersion.CURRENT.value,
            "service.name" to "demo-service",
            "store" to "demo-store",
            "store_id" to "demo-store-id",
            "run_id" to "demo-run-id",
        )
    }

    "resource attributes default to meta when no extras provided" {
        val payload = snapshot.toOtlpPayload()

        payload.resourceMetrics
            .single()
            .resource!!
            .attributes
            .associate { it.key to it.value.stringValue } shouldBe mapOf(
            "schema.version" to MetricsSchemaVersion.CURRENT.value,
            "store" to "demo-store",
            "store_id" to "demo-store-id",
            "run_id" to "demo-run-id",
        )
    }

    "openmetrics output includes schema version label" {
        val buffer = StringBuilder()
        val sink = OpenMetricsSink(delegate = AppendableStringSink(buffer), includeTimestamp = true)

        sink.emit(snapshot)

        buffer.toString() shouldContain """schema_version="${MetricsSchemaVersion.CURRENT.value}""""
        buffer.toString() shouldContain """run_id="demo-run-id""""
    }

    "metric names stay stable" {
        val payload = snapshot.toOtlpPayload(namespace = "flowmvi")
        val names = payload.resourceMetrics.single().scopeMetrics.single().metrics.map { it.name }

        names shouldContainExactly listOf(
            "flowmvi_config_window_seconds",
            "flowmvi_config_ema_alpha",
            "flowmvi_config_schema_version",
            "flowmvi_intents_total",
            "flowmvi_intents_processed_total",
            "flowmvi_intents_dropped_total",
            "flowmvi_intents_undelivered_total",
            "flowmvi_intents_ops_per_second",
            "flowmvi_intents_duration_seconds_avg",
            "flowmvi_intents_duration_seconds",
            "flowmvi_intents_queue_time_seconds_avg",
            "flowmvi_intents_in_flight_max",
            "flowmvi_intents_inter_arrival_seconds_avg",
            "flowmvi_intents_inter_arrival_seconds_median",
            "flowmvi_intents_burst_max",
            "flowmvi_intents_buffer_max_occupancy",
            "flowmvi_intents_buffer_overflows_total",
            "flowmvi_intents_plugin_overhead_seconds_avg",
            "flowmvi_intents_plugin_overhead_seconds_median",
            "flowmvi_actions_sent_total",
            "flowmvi_actions_delivered_total",
            "flowmvi_actions_undelivered_total",
            "flowmvi_actions_ops_per_second",
            "flowmvi_actions_delivery_seconds_avg",
            "flowmvi_actions_delivery_seconds",
            "flowmvi_actions_queue_time_seconds_avg",
            "flowmvi_actions_queue_time_seconds_median",
            "flowmvi_actions_buffer_max_occupancy",
            "flowmvi_actions_buffer_overflows_total",
            "flowmvi_actions_plugin_overhead_seconds_avg",
            "flowmvi_actions_plugin_overhead_seconds_median",
            "flowmvi_state_transitions_total",
            "flowmvi_state_transitions_vetoed_total",
            "flowmvi_state_update_seconds_avg",
            "flowmvi_state_update_seconds",
            "flowmvi_state_ops_per_second",
            "flowmvi_subscriptions_events_total",
            "flowmvi_subscriptions_current",
            "flowmvi_subscriptions_peak",
            "flowmvi_subscriptions_lifetime_seconds_avg",
            "flowmvi_subscriptions_lifetime_seconds_median",
            "flowmvi_subscriptions_avg",
            "flowmvi_subscriptions_median",
            "flowmvi_lifecycle_start_total",
            "flowmvi_lifecycle_stop_total",
            "flowmvi_lifecycle_uptime_seconds_total",
            "flowmvi_lifecycle_lifetime_seconds_current",
            "flowmvi_lifecycle_lifetime_seconds_avg",
            "flowmvi_lifecycle_lifetime_seconds_median",
            "flowmvi_lifecycle_bootstrap_seconds_avg",
            "flowmvi_lifecycle_bootstrap_seconds_median",
            "flowmvi_exceptions_total",
            "flowmvi_exceptions_handled_total",
            "flowmvi_exceptions_recovery_seconds_avg",
            "flowmvi_exceptions_recovery_seconds_median",
        )
    }

    "quantiles are rendered as gauge datapoints with quantile label" {
        val payload = snapshot.toOtlpPayload(namespace = "flowmvi")
        val metrics = payload.resourceMetrics.single().scopeMetrics.single().metrics.associateBy { it.name }
        val metric = metrics.getValue("flowmvi_intents_duration_seconds")
        val dataPoints = metric.gauge!!.dataPoints

        dataPoints.map { it.quantileLabel() } shouldBe listOf("0.5", "0.9", "0.95", "0.99")
        dataPoints.map { it.asDouble } shouldBe listOf(0.01, 0.02, 0.025, 0.03)
        metric.unit shouldBe "s"
    }

    "cumulative counters keep stable start time from meta" {
        val payload = snapshot.toOtlpPayload(namespace = "flowmvi")
        val metrics = payload.resourceMetrics.single().scopeMetrics.single().metrics.associateBy { it.name }
        val metric = metrics.getValue("flowmvi_intents_total")
        val dataPoint = metric.sum!!.dataPoints.single()
        dataPoint.startTimeUnixNano shouldBe snapshot.meta.startTime!!.toEpochMilliseconds() * 1_000_000
        dataPoint.timeUnixNano shouldBe snapshot.meta.generatedAt.toEpochMilliseconds() * 1_000_000
    }

    "config schema version is exposed as numeric gauge" {
        val payload = snapshot.toOtlpPayload(namespace = "flowmvi")
        val metric = payload.resourceMetrics
            .single()
            .scopeMetrics
            .single()
            .metrics
            .first { it.name == "flowmvi_config_schema_version" }

        metric.gauge!!.dataPoints.single().asDouble shouldBe 1.0
    }

    "sink serializes NaN values when present" {
        val buffer = StringBuilder()
        val sink = OtlpJsonMetricsSink(
            delegate = AppendableStringSink(buffer),
            json = json
        )
        val weirdSnapshot = snapshot.copy(intents = snapshot.intents.copy(opsPerSecond = Double.NaN))

        sink.emit(weirdSnapshot)

        val element = json.parseToJsonElement(buffer.toString()).jsonObject
        val resource = element["resourceMetrics"]!!.jsonArray.first().jsonObject
        val scope = resource["scopeMetrics"]!!.jsonArray.first().jsonObject
        val metrics = scope["metrics"]!!.jsonArray
        val opsMetric = metrics.first {
            it.jsonObject["name"]!!.jsonPrimitive.content == "flowmvi_intents_ops_per_second"
        }
        val dataPoints = opsMetric.jsonObject["gauge"]!!
            .jsonObject["dataPoints"]!!
            .jsonArray
        val value = dataPoints
            .first()
            .jsonObject["asDouble"]!!.jsonPrimitive.content

        value.shouldContainIgnoringCase("nan")
    }

    "resourceAttributesProvider overrides and merges attrs" {
        val payload = snapshot.toOtlpPayload(
            resourceAttributesProvider = { meta ->
                mapOf(
                    "service.name" to "svc-from-provider",
                    "store_id" to "overridden-id", // should override meta.storeId default
                    "custom" to "value"
                )
            }
        )
        val attrs = payload.resourceMetrics.single().resource!!.attributes.associate { it.key to it.value.stringValue }

        attrs shouldBe mapOf(
            "custom" to "value",
            "schema.version" to MetricsSchemaVersion.CURRENT.value,
            "service.name" to "svc-from-provider",
            "store" to "demo-store",
            "store_id" to "overridden-id",
            "run_id" to "demo-run-id",
        )
    }

    "surfaceVersion override is honored in schema version attribute" {
        val payload = snapshot.toOtlpPayload(
            namespace = "flowmvi",
            surfaceVersion = MetricsSchemaVersion(2, 0)
        )
        val attrs = payload.resourceMetrics.single().resource!!.attributes.associate { it.key to it.value.stringValue }

        attrs["schema.version"] shouldBe "2.0"
    }

    "namespace and scope name are applied" {
        val payload = snapshot.toOtlpPayload(namespace = "custom", scopeName = "flowmvi.custom")
        val scopeMetrics = payload.resourceMetrics.single().scopeMetrics.single()

        scopeMetrics.scope!!.name shouldBe "flowmvi.custom"
        scopeMetrics.metrics.all { it.name.startsWith("custom_") } shouldBe true
    }

    "temporality selection is honored for sums" {
        val payload = snapshot.toOtlpPayload(
            temporality = AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA
        )
        val sums = payload.resourceMetrics.single().scopeMetrics.single().metrics.mapNotNull { it.sum }

        sums.all { it.aggregationTemporality == AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA } shouldBe true
        sums.all { it.isMonotonic } shouldBe true
    }

    "fixedTimestamp overrides generatedAt when startTime is absent" {
        val fixed = Instant.fromEpochMilliseconds(70_000)
        val noStart = sampleSnapshot(
            meta = snapshot.meta.copy(
                startTime = null,
                generatedAt = Instant.fromEpochMilliseconds(1)
            )
        )

        val payload = noStart.toOtlpPayload(
            fixedTimestamp = fixed,
            temporality = AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE
        )

        val metrics = payload.resourceMetrics.single().scopeMetrics.single().metrics
        val gaugePoint = metrics.first { it.gauge != null }.gauge!!.dataPoints.single()
        val sumPoint = metrics.first { it.sum != null }.sum!!.dataPoints.single()
        val expectedTimestamp = fixed.toEpochMilliseconds() * 1_000_000
        val expectedStart = expectedTimestamp - 60 * 1_000_000_000L

        gaugePoint.startTimeUnixNano shouldBe expectedTimestamp
        gaugePoint.timeUnixNano shouldBe expectedTimestamp
        sumPoint.timeUnixNano shouldBe expectedTimestamp
        sumPoint.startTimeUnixNano shouldBe expectedStart
    }

    "surfaceVersion override downgrades schema attribute" {
        val target = MetricsSchemaVersion(0, 9)

        val payload = snapshot.toOtlpPayload(surfaceVersion = target)

        payload.resourceMetrics.single().resource!!.attributes.associate { it.key to it.value.stringValue }["schema.version"] shouldBe "0.9"
    }

    "resource attributes are sorted lexicographically after merge" {
        val payload = snapshot.toOtlpPayload(
            resourceAttributes = mapOf("z-key" to "z", "a-key" to "a")
        )
        val keys = payload.resourceMetrics.single().resource!!.attributes.map { it.key }

        keys shouldBe keys.sorted()
    }

    "gauges render NaN and infinities as doubles" {
        val odd = snapshot.copy(
            intents = snapshot.intents.copy(opsPerSecond = Double.NaN),
            actions = snapshot.actions.copy(opsPerSecond = Double.POSITIVE_INFINITY),
            state = snapshot.state.copy(opsPerSecond = Double.NEGATIVE_INFINITY),
        )

        val payload = odd.toOtlpPayload(namespace = "flowmvi")
        val metrics = payload.resourceMetrics.single().scopeMetrics.single().metrics.associateBy { it.name }

        metrics.getValue("flowmvi_intents_ops_per_second").gauge!!.dataPoints.single().asDouble!!.isNaN() shouldBe true
        metrics.getValue("flowmvi_actions_ops_per_second").gauge!!.dataPoints.single().asDouble shouldBe Double.POSITIVE_INFINITY
        metrics.getValue("flowmvi_state_ops_per_second").gauge!!.dataPoints.single().asDouble shouldBe Double.NEGATIVE_INFINITY
    }

    "delta temporality uses meta startTime when present" {
        val start = Instant.fromEpochMilliseconds(123_000)
        val fixedMeta = snapshot.meta.copy(startTime = start)
        val payload = snapshot.copy(meta = fixedMeta).toOtlpPayload(
            temporality = AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA
        )
        val sumPoint = payload.resourceMetrics.single().scopeMetrics.single().metrics
            .first { it.sum != null }
            .sum!!
            .dataPoints
            .single()

        sumPoint.startTimeUnixNano shouldBe start.toEpochMilliseconds() * 1_000_000
    }
})

private fun OtlpNumberDataPoint.quantileLabel(): String? = attributes
    .firstOrNull { it.key == "quantile" }
    ?.value
    ?.stringValue
