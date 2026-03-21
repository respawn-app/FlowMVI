@file:OptIn(ExperimentalMaterial3Api::class)

package pro.respawn.flowmvi.sample.features.toplevelcompose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.sample.Res
import pro.respawn.flowmvi.sample.arch.di.container
import pro.respawn.flowmvi.sample.navigation.AppNavigator
import pro.respawn.flowmvi.sample.navigation.util.backNavigator
import pro.respawn.flowmvi.sample.toplevel_compose_clock_title
import pro.respawn.flowmvi.sample.toplevel_compose_feature_title
import pro.respawn.flowmvi.sample.toplevel_compose_refresh_button
import pro.respawn.flowmvi.sample.toplevel_compose_weather_title
import pro.respawn.flowmvi.sample.ui.widgets.CodeText
import pro.respawn.flowmvi.sample.ui.widgets.RScaffold
import pro.respawn.flowmvi.sample.util.formatAsMultiline
import pro.respawn.kmmutils.compose.resources.string

private const val Description = """
    This screen demonstrates top-level composition using the transitions plugin.
    \n\n
    Two child stores (Weather & Clock) are composed into the parent at the top level.
    Their state is always merged into the parent's DashboardState, regardless of which 
    state the parent is in.
    \n\n
    The Weather store loads data with a simulated delay, while the Clock store 
    updates every second using whileSubscribed.
"""

//language=kotlin
private const val Code = """
transitions {
    compose(weatherStore) { copy(weather = it) }
    compose(clockStore) { copy(clock = it) }

    state<DashboardState> {
        on<ClickedRefresh> {
            weatherStore.intent(WeatherIntent.Refresh)
        }
    }
}
"""

@Composable
internal fun TopLevelComposeScreen(
    navigator: AppNavigator,
) = with(container<TopLevelComposeContainer, _, _, _>()) {
    val state by subscribe()

    RScaffold(
        onBack = navigator.backNavigator,
        title = Res.string.toplevel_compose_feature_title.string(),
    ) {
        TopLevelComposeScreenContent(state)
    }
}

@Composable
private fun IntentReceiver<DashboardIntent>.TopLevelComposeScreenContent(
    state: DashboardState,
) = Column(
    modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
        .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Top,
) {
    Text(Description.formatAsMultiline(), modifier = Modifier.widthIn(max = 600.dp))
    Spacer(Modifier.height(12.dp))
    CodeText(Code)
    Spacer(Modifier.height(24.dp))

    // Weather card
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = Res.string.toplevel_compose_weather_title.string(),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))
            when (val weather = state.weather) {
                is WeatherState.Loading -> CircularProgressIndicator()
                is WeatherState.Loaded -> {
                    Text(
                        text = "${weather.temperature}°C — ${weather.condition}",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    // Clock card
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = Res.string.toplevel_compose_clock_title.string(),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.clock.time,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
    }

    Spacer(Modifier.height(24.dp))

    Button(onClick = { intent(DashboardIntent.ClickedRefresh) }) {
        Text(Res.string.toplevel_compose_refresh_button.string())
    }

    Spacer(Modifier.navigationBarsPadding())
}
