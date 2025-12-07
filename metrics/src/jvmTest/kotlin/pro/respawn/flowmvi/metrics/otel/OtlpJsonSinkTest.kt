package pro.respawn.flowmvi.metrics.otel

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContainIgnoringCase
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import pro.respawn.flowmvi.metrics.AppendableStringSink
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
            "service.name" to "demo-service",
            "store" to "demo-store",
            "store_id" to "demo-store-id"
        )
    }

    "metric names stay stable" {
        val payload = snapshot.toOtlpPayload(namespace = "flowmvi")
        val names = payload.resourceMetrics.single().scopeMetrics.single().metrics.map { it.name }

        names shouldContainExactly listOf(
            "flowmvi_config_window_seconds",
            "flowmvi_config_ema_alpha",
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
            "service.name" to "svc-from-provider",
            "store" to "demo-store",
            "store_id" to "overridden-id",
        )
    }

}) 

private fun sampleSnapshot(): MetricsSnapshot = MetricsSnapshot(
    meta = Meta(
        generatedAt = Instant.fromEpochMilliseconds(1_700_000_000_000),
        startTime = Instant.fromEpochMilliseconds(1_699_999_000_000),
        storeName = "demo-store",
        storeId = "demo-store-id",
        windowSeconds = 60,
        emaAlpha = 0.5f
    ),
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

private fun OtlpNumberDataPoint.quantileLabel(): String? = attributes
    .firstOrNull { it.key == "quantile" }
    ?.value
    ?.stringValue
