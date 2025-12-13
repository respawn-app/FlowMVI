package pro.respawn.flowmvi.debugger.server.ui.screens.storemetrics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.koin.core.parameter.parametersOf
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.requireLifecycle
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.compose.preview.EmptyReceiver
import pro.respawn.flowmvi.debugger.server.StoreKey
import pro.respawn.flowmvi.debugger.server.di.container
import pro.respawn.flowmvi.debugger.server.ui.icons.Close
import pro.respawn.flowmvi.debugger.server.ui.icons.Icons
import pro.respawn.flowmvi.debugger.server.ui.screens.storemetrics.StoreMetricsIntent.WindowSelected
import pro.respawn.flowmvi.debugger.server.ui.theme.RespawnTheme
import pro.respawn.flowmvi.debugger.server.ui.widgets.Dot
import pro.respawn.flowmvi.debugger.server.ui.widgets.DropDownAction
import pro.respawn.flowmvi.debugger.server.ui.widgets.RDropDownMenu
import pro.respawn.flowmvi.debugger.server.ui.widgets.RErrorView
import pro.respawn.flowmvi.debugger.server.ui.widgets.RScaffold
import pro.respawn.flowmvi.debugger.server.ui.widgets.TypeCrossfade
import pro.respawn.flowmvi.debugger.server.ui.widgets.charts.Line
import pro.respawn.flowmvi.debugger.server.ui.widgets.charts.RLineChart
import pro.respawn.flowmvi.debugger.server.ui.widgets.charts.StyledLine
import pro.respawn.flowmvi.debugger.server.ui.widgets.charts.XAxis
import pro.respawn.flowmvi.debugger.server.ui.widgets.charts.YAxis
import pro.respawn.kmmutils.common.toFloat
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.Instant
import kotlin.uuid.Uuid

private const val YAxisLabelCount: Int = 3
private const val YAxisMaxWidthDp: Int = 72
private const val ChartHeightDp: Int = 260

private const val XLabelAlwaysDrawThreshold: Int = 6
private const val XLabelTargetCount: Int = 5

private const val SecondsInMinute: Int = 60

private const val RateSmallThreshold: Float = 10f
private const val RateMediumThreshold: Float = 100f

private const val MillisInSecond: Float = 1000f
private const val MillisLargeThreshold: Float = 10_000f

private const val CountK: Float = 1_000f
private const val CountM: Float = 1_000_000f

private val SeriesColors: List<Color> = listOf(
    Color(0xFF22C55E),
    Color(0xFF4F7DFF),
    Color(0xFFF59E0B),
    Color(0xFFEF4444),
)

@Composable
internal fun StoreMetricsPage(
    key: StoreKey,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) = with(container<StoreMetricsContainer, _, _, _> { parametersOf(key) }) {
    val state by subscribe(requireLifecycle()) { /* no-op */ }
    StoreMetricsPageContent(state = state, onClose = onClose, modifier = modifier)
}

@Composable
private fun IntentReceiver<StoreMetricsIntent>.StoreMetricsPageContent(
    state: StoreMetricsState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) = TypeCrossfade(state) {
    when (this) {
        is StoreMetricsState.Error -> RErrorView(e)
        is StoreMetricsState.Loading -> CircularProgressIndicator()
        is StoreMetricsState.Disconnected -> Text("Disconnected. No metrics available.")
        is StoreMetricsState.DisplayingMetrics -> StoreMetricsContent(
            state = this,
            onClose = onClose,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IntentReceiver<StoreMetricsIntent>.StoreMetricsContent(
    state: StoreMetricsState.DisplayingMetrics,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier.fillMaxSize() then modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.name ?: state.id.toString(),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (state.name != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.id.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(
                onClick = onClose,
                colors = IconButtonDefaults.filledIconButtonColors(MaterialTheme.colorScheme.surfaceVariant),
            ) { Icon(Icons.Close, contentDescription = null) }
        }
        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            MetricSelector(selected = state.selected)
            WindowSlider(
                seconds = state.windowSeconds,
                modifier = Modifier.weight(1f),
            )
        }

        val chart = state.chart
        if (chart == null || chart.series.isEmpty()) {
            Text("No metrics received yet.")
            return
        }

        val pointCount = chart.series.first().points.size
        val xEvery = remember(pointCount) { xLabelEvery(pointCount) }
        val xAxis = remember(chart.start, xEvery) {
            XAxis<Instant>(
                drawXLabelEvery = xEvery,
                labelFormatter = { (instant, _), _ -> formatElapsed(instant - chart.start) }
            )
        }

        val yFormatter = remember(chart.metric) { yAxisFormatter(chart.metric) }

        val yAxis = remember(yFormatter, chart.minY, chart.maxY) {
            YAxis(
                maxWidth = YAxisMaxWidthDp.dp,
                labelAmount = YAxisLabelCount,
                labelFormatter = yFormatter,
            )
        }

        val lines = remember(chart) {
            chart.series.mapIndexed { index, series ->
                StyledLine(
                    line = Line(points = series.points, minValue = chart.minY, maxValue = chart.maxY),
                    color = SeriesColors[index % SeriesColors.size],
                )
            }
        }

        RLineChart(
            modifier = Modifier.fillMaxWidth().height(ChartHeightDp.dp),
            lines = lines,
            dotsRadius = 0.dp,
            yAxis = yAxis,
            xAxis = xAxis,
            animationKeys = arrayOf<Any>(state.selected, state.windowSeconds)
        )

        Legend(series = chart.series, colors = lines.map { it.color })
    }
}

@Composable
private fun IntentReceiver<StoreMetricsIntent>.MetricSelector(
    selected: StoreMetricChart,
) = RDropDownMenu(
    button = {
        OutlinedButton(
            onClick = { toggle() },
            modifier = Modifier.animateContentSize(),
        ) { Text(metricLabel(selected)) }
    },
    actions = {
        StoreMetricChart.entries.forEach { metric ->
            DropDownAction(
                text = metricLabel(metric),
                onClick = { intent(StoreMetricsIntent.MetricSelected(metric)) },
            )
        }
    },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntentReceiver<StoreMetricsIntent>.WindowSlider(
    seconds: Int,
    modifier: Modifier = Modifier,
) {
    val clamped = seconds.coerceIn(MetricsWindowRange)
    var sliderValue by remember(clamped) { mutableFloatStateOf(clamped.toFloat()) }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Window: ${sliderValue.roundToInt()}s",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val source = remember { MutableInteractionSource() }
        Slider(
            value = sliderValue,
            interactionSource = source,
            onValueChange = { sliderValue = it },
            thumb = { SliderDefaults.Thumb(source, thumbSize = DpSize(4.dp, 32.dp)) },
            valueRange = MetricsWindowRange.toFloat(),
            onValueChangeFinished = { intent(WindowSelected(sliderValue.roundToInt())) },
        )
    }
}

@Composable
private fun Legend(
    series: List<MetricSeriesData>,
    colors: List<Color>,
) = AnimatedVisibility(series.isNotEmpty()) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        series.forEachIndexed { index, s ->
            val color = colors.getOrNull(index) ?: MaterialTheme.colorScheme.primary
            Row(verticalAlignment = Alignment.CenterVertically) {
                Dot(color = color)
                Spacer(Modifier.width(6.dp))
                Text(text = seriesLabel(s.id), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun metricLabel(metric: StoreMetricChart): String = when (metric) {
    StoreMetricChart.IntentsOpsPerSecond -> "Intents ops/sec"
    StoreMetricChart.ActionsOpsPerSecond -> "Actions ops/sec"
    StoreMetricChart.StateOpsPerSecond -> "State ops/sec"
    StoreMetricChart.IntentDurations -> "Intent duration"
    StoreMetricChart.ActionDeliveryLatencies -> "Action delivery latency"
    StoreMetricChart.StateUpdateDurations -> "State update duration"
    StoreMetricChart.Subscriptions -> "Subscriptions"
    StoreMetricChart.Exceptions -> "Exceptions"
    StoreMetricChart.Buffers -> "Buffers"
}

@Composable
private fun seriesLabel(series: StoreMetricSeries): String = when (series) {
    StoreMetricSeries.OpsPerSecond -> "ops/sec"
    StoreMetricSeries.Avg -> "avg"
    StoreMetricSeries.P50 -> "p50"
    StoreMetricSeries.P90 -> "p90"
    StoreMetricSeries.P99 -> "p99"
    StoreMetricSeries.Current -> "current"
    StoreMetricSeries.Peak -> "peak"
    StoreMetricSeries.Total -> "total"
    StoreMetricSeries.Handled -> "handled"
    StoreMetricSeries.IntentBufferMaxOccupancy -> "intent occupancy"
    StoreMetricSeries.ActionBufferMaxOccupancy -> "action occupancy"
}

private fun yAxisFormatter(metric: StoreMetricChart): (Float) -> String = when (metric) {
    StoreMetricChart.IntentsOpsPerSecond,
    StoreMetricChart.ActionsOpsPerSecond,
    StoreMetricChart.StateOpsPerSecond -> ::formatRate

    StoreMetricChart.IntentDurations,
    StoreMetricChart.ActionDeliveryLatencies,
    StoreMetricChart.StateUpdateDurations -> ::formatMillis

    StoreMetricChart.Subscriptions,
    StoreMetricChart.Exceptions,
    StoreMetricChart.Buffers -> ::formatCount
}

private fun xLabelEvery(pointCount: Int): Int {
    if (pointCount <= XLabelAlwaysDrawThreshold) return 1
    return (pointCount / XLabelTargetCount).coerceAtLeast(1)
}

private fun formatElapsed(d: Duration): String {
    val s = d.toDouble(DurationUnit.SECONDS).roundToInt().coerceAtLeast(0)
    val m = s / SecondsInMinute
    val rem = s % SecondsInMinute
    return if (m == 0) "${rem}s" else "%d:%02d".format(m, rem)
}

private fun formatRate(v: Float): String = when {
    v < RateSmallThreshold -> "%.2f/s".format(v)
    v < RateMediumThreshold -> "%.1f/s".format(v)
    else -> "${v.roundToInt()}/s"
}

private fun formatMillis(v: Float): String = when {
    v >= MillisLargeThreshold -> "${(v / MillisInSecond).roundToInt()}s"
    v >= MillisInSecond -> "%.1fs".format(v / MillisInSecond)
    else -> "${v.roundToInt()}ms"
}

private fun formatCount(v: Float): String {
    val n = v.coerceAtLeast(0f)
    return when {
        n >= CountM -> "%.1fM".format(n / CountM)
        n >= CountK -> "%.1fk".format(n / CountK)
        else -> n.roundToInt().toString()
    }
}

@Composable
@androidx.compose.desktop.ui.tooling.preview.Preview
private fun StoreMetricsPagePreview() = RespawnTheme {
    RScaffold {
        val now = Clock.System.now()
        val start = now - 120.seconds

        fun points(vararg values: Float) = values.mapIndexed { i, v -> start + (i * 10).seconds to v }.toImmutableList()

        val chart = StoreMetricsChartModel(
            metric = StoreMetricChart.IntentDurations,
            windowSeconds = 120,
            start = start,
            series = persistentListOf(
                MetricSeriesData(StoreMetricSeries.Avg, points(12f, 15f, 11f, 18f, 14f)),
                MetricSeriesData(StoreMetricSeries.P90, points(22f, 25f, 21f, 28f, 24f)),
            ),
            minY = 10f,
            maxY = 30f,
        )

        EmptyReceiver {
            StoreMetricsContent(
                state = StoreMetricsState.DisplayingMetrics(
                    id = Uuid.random(),
                    name = "SampleStore",
                    connected = true,
                    metrics = persistentListOf(),
                    selected = chart.metric,
                    windowSeconds = chart.windowSeconds,
                    chart = chart,
                ),
                onClose = {},
            )
        }
    }
}
