package pro.respawn.flowmvi.sample.features.toplevelcompose

import androidx.compose.runtime.Immutable
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
internal sealed interface WeatherState : MVIState {

    data object Loading : WeatherState
    data class Loaded(val temperature: Int, val condition: String) : WeatherState
}

@Immutable
internal data class ClockState(val time: String = "--:--:--") : MVIState

// Parent state — single data class, always-active composition target
@Immutable
internal data class DashboardState(
    val weather: WeatherState = WeatherState.Loading,
    val clock: ClockState = ClockState(),
) : MVIState

@Immutable
internal sealed interface DashboardIntent : MVIIntent {

    data object ClickedRefresh : DashboardIntent
}

@Immutable
internal sealed interface DashboardAction : MVIAction
