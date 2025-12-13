package pro.respawn.flowmvi.debugger.server.ui.screens.storemetrics

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.debugger.server.DebugServer
import pro.respawn.flowmvi.debugger.server.ServerState
import pro.respawn.flowmvi.debugger.server.StoreKey
import pro.respawn.flowmvi.debugger.server.arch.configuration.StoreConfiguration
import pro.respawn.flowmvi.debugger.server.arch.configuration.configure
import pro.respawn.flowmvi.debugger.server.ui.screens.storemetrics.StoreMetricsIntent.MetricSelected
import pro.respawn.flowmvi.debugger.server.ui.screens.storemetrics.StoreMetricsIntent.WindowSelected
import pro.respawn.flowmvi.debugger.server.ui.screens.storemetrics.StoreMetricsState.DisplayingMetrics
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.plugins.delegate.DelegationMode
import pro.respawn.flowmvi.plugins.delegate.delegate
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed
import pro.respawn.flowmvi.util.typed

private typealias Ctx = PipelineContext<StoreMetricsState, StoreMetricsIntent, StoreMetricsAction>

@OptIn(ExperimentalFlowMVIAPI::class)
internal class StoreMetricsContainer(
    private val storeKey: StoreKey,
    configuration: StoreConfiguration,
) : Container<StoreMetricsState, StoreMetricsIntent, StoreMetricsAction> {

    override val store = store(StoreMetricsState.Loading) {
        configure(configuration, "StoreMetricsStore")
        val serverState by delegate(DebugServer.store, DelegationMode.Immediate(), start = false)

        recover {
            updateState { StoreMetricsState.Error(it) }
            null
        }

        whileSubscribed {
            serverState.onEach { state ->
                updateState {
                    val current = typed<DisplayingMetrics>()
                    when (state) {
                        is ServerState.Error -> StoreMetricsState.Error(state.e)
                        is ServerState.Idle -> StoreMetricsState.Disconnected
                        is ServerState.Running -> state.clients[storeKey]?.run {
                            val selected = current?.selected ?: StoreMetricChart.IntentsOpsPerSecond
                            val windowSeconds = current?.windowSeconds ?: 300
                            DisplayingMetrics(
                                id = id,
                                name = name,
                                connected = isConnected,
                                metrics = metrics,
                                selected = selected,
                                windowSeconds = windowSeconds,
                                chart = buildChartModel(metrics, selected, windowSeconds),
                            )
                        } ?: StoreMetricsState.Disconnected
                    }
                }
            }.consume(Dispatchers.Default)
        }

        reduce { intent ->
            when (intent) {
                is MetricSelected -> updateState<DisplayingMetrics, _> {
                    copy(
                        selected = intent.metric,
                        chart = buildChartModel(metrics, intent.metric, windowSeconds),
                    )
                }
                is WindowSelected -> updateState<DisplayingMetrics, _> {
                    val clamped = intent.seconds.coerceIn(MetricsMinWindowSeconds, MetricsMaxWindowSeconds)
                    copy(windowSeconds = clamped, chart = buildChartModel(metrics, selected, clamped))
                }
            }
        }
    }
}
