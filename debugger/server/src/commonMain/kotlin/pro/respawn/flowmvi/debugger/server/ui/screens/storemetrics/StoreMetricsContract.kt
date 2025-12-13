package pro.respawn.flowmvi.debugger.server.ui.screens.storemetrics

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.metrics.api.MetricsSnapshot
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal const val MetricsMaxWindowSeconds: Int = 600
internal const val MetricsMinWindowSeconds: Int = 30
internal val MetricsWindowRange = MetricsMinWindowSeconds..MetricsMaxWindowSeconds

@Immutable
internal enum class StoreMetricChart {

    IntentsOpsPerSecond,
    ActionsOpsPerSecond,
    StateOpsPerSecond,
    IntentDurations,
    ActionDeliveryLatencies,
    StateUpdateDurations,
    Subscriptions,
    Exceptions,
    Buffers,
}

@Immutable
internal enum class StoreMetricSeries {

    OpsPerSecond,
    Avg,
    P50,
    P90,
    P99,
    Current,
    Peak,
    Total,
    Handled,
    IntentBufferMaxOccupancy,
    ActionBufferMaxOccupancy,
}

@Immutable
internal data class MetricSeriesData(
    val id: StoreMetricSeries,
    val points: ImmutableList<Pair<Instant, Float>>,
)

@Immutable
internal data class StoreMetricsChartModel(
    val metric: StoreMetricChart,
    val windowSeconds: Int,
    val start: Instant,
    val series: ImmutableList<MetricSeriesData>,
    val minY: Float,
    val maxY: Float,
)

@Immutable
internal sealed interface StoreMetricsState : MVIState {

    data object Loading : StoreMetricsState
    data object Disconnected : StoreMetricsState
    data class Error(val e: Exception) : StoreMetricsState

    data class DisplayingMetrics(
        val id: Uuid,
        val name: String?,
        val connected: Boolean,
        val metrics: ImmutableList<MetricsSnapshot>,
        val selected: StoreMetricChart = StoreMetricChart.IntentsOpsPerSecond,
        val windowSeconds: Int = 300,
        val chart: StoreMetricsChartModel? = null,
    ) : StoreMetricsState
}

@Immutable
internal sealed interface StoreMetricsIntent : MVIIntent {

    data class MetricSelected(val metric: StoreMetricChart) : StoreMetricsIntent
    data class WindowSelected(val seconds: Int) : StoreMetricsIntent
}

@Immutable
internal sealed interface StoreMetricsAction : MVIAction
