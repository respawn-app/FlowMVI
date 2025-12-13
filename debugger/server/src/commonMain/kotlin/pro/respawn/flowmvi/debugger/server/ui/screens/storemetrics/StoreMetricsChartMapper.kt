package pro.respawn.flowmvi.debugger.server.ui.screens.storemetrics

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import pro.respawn.flowmvi.metrics.api.MetricsSnapshot
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

private const val FlatRangePadding: Float = 1f
private const val FloatEpsilon: Float = 1e-6f

internal fun buildChartModel(
    metrics: ImmutableList<MetricsSnapshot>,
    metric: StoreMetricChart,
    windowSeconds: Int,
): StoreMetricsChartModel? {
    val windowed = metrics.toWindow(windowSeconds = windowSeconds)
    if (windowed.isEmpty()) return null

    val seriesSpecs = seriesSpecs(metric)
    val series = seriesSpecs.map { spec ->
        MetricSeriesData(
            id = spec.id,
            points = windowed.map { it.meta.generatedAt to spec.value(it) }.toImmutableList(),
        )
    }.toImmutableList()

    val (minY, maxY) = computeMinMax(series)
    return StoreMetricsChartModel(
        metric = metric,
        windowSeconds = windowSeconds.coerceIn(MetricsMinWindowSeconds, MetricsMaxWindowSeconds),
        start = windowed.first().meta.generatedAt,
        series = series,
        minY = minY,
        maxY = maxY,
    )
}

private data class SeriesSpec(
    val id: StoreMetricSeries,
    val value: (MetricsSnapshot) -> Float,
)

private fun seriesSpecs(metric: StoreMetricChart): List<SeriesSpec> = when (metric) {
    StoreMetricChart.IntentsOpsPerSecond -> listOf(
        SeriesSpec(StoreMetricSeries.OpsPerSecond) { it.intents.opsPerSecond.toFloat() },
    )
    StoreMetricChart.ActionsOpsPerSecond -> listOf(
        SeriesSpec(StoreMetricSeries.OpsPerSecond) { it.actions.opsPerSecond.toFloat() },
    )
    StoreMetricChart.StateOpsPerSecond -> listOf(
        SeriesSpec(StoreMetricSeries.OpsPerSecond) { it.state.opsPerSecond.toFloat() },
    )
    StoreMetricChart.IntentDurations -> listOf(
        SeriesSpec(StoreMetricSeries.Avg) { it.intents.durationAvg.inWholeMilliseconds.toFloat() },
        SeriesSpec(StoreMetricSeries.P50) { it.intents.durationP50.inWholeMilliseconds.toFloat() },
        SeriesSpec(StoreMetricSeries.P90) { it.intents.durationP90.inWholeMilliseconds.toFloat() },
        SeriesSpec(StoreMetricSeries.P99) { it.intents.durationP99.inWholeMilliseconds.toFloat() },
    )
    StoreMetricChart.ActionDeliveryLatencies -> listOf(
        SeriesSpec(StoreMetricSeries.Avg) { it.actions.deliveryAvg.inWholeMilliseconds.toFloat() },
        SeriesSpec(StoreMetricSeries.P50) { it.actions.deliveryP50.inWholeMilliseconds.toFloat() },
        SeriesSpec(StoreMetricSeries.P90) { it.actions.deliveryP90.inWholeMilliseconds.toFloat() },
        SeriesSpec(StoreMetricSeries.P99) { it.actions.deliveryP99.inWholeMilliseconds.toFloat() },
    )
    StoreMetricChart.StateUpdateDurations -> listOf(
        SeriesSpec(StoreMetricSeries.Avg) { it.state.updateAvg.inWholeMilliseconds.toFloat() },
        SeriesSpec(StoreMetricSeries.P50) { it.state.updateP50.inWholeMilliseconds.toFloat() },
        SeriesSpec(StoreMetricSeries.P90) { it.state.updateP90.inWholeMilliseconds.toFloat() },
        SeriesSpec(StoreMetricSeries.P99) { it.state.updateP99.inWholeMilliseconds.toFloat() },
    )
    StoreMetricChart.Subscriptions -> listOf(
        SeriesSpec(StoreMetricSeries.Current) { it.subscriptions.current.toFloat() },
        SeriesSpec(StoreMetricSeries.Peak) { it.subscriptions.peak.toFloat() },
    )
    StoreMetricChart.Exceptions -> listOf(
        SeriesSpec(StoreMetricSeries.Total) { it.exceptions.total.toFloat() },
        SeriesSpec(StoreMetricSeries.Handled) { it.exceptions.handled.toFloat() },
    )
    StoreMetricChart.Buffers -> listOf(
        SeriesSpec(StoreMetricSeries.IntentBufferMaxOccupancy) { it.intents.bufferMaxOccupancy.toFloat() },
        SeriesSpec(StoreMetricSeries.ActionBufferMaxOccupancy) { it.actions.bufferMaxOccupancy.toFloat() },
    )
}

private fun computeMinMax(series: ImmutableList<MetricSeriesData>): Pair<Float, Float> {
    val values = series.asSequence().flatMap { it.points.asSequence().map { p -> p.second } }.toList()
    val min = values.minOrNull() ?: 0f
    val max = values.maxOrNull() ?: 0f
    if (min == max) return min to max + FlatRangePadding
    val range = abs(max - min)
    if (range < FloatEpsilon) return min to min + FlatRangePadding
    return min to max
}

private fun ImmutableList<MetricsSnapshot>.toWindow(windowSeconds: Int): List<MetricsSnapshot> {
    val clamped = windowSeconds.coerceIn(MetricsMinWindowSeconds, MetricsMaxWindowSeconds)
    val sorted = this.sortedBy { it.meta.generatedAt }
    val latest = sorted.lastOrNull()?.meta?.generatedAt ?: return emptyList()
    val from = latest - clamped.seconds
    return sorted
        .asSequence()
        .filter { it.meta.generatedAt >= from }
        .toList()
}
