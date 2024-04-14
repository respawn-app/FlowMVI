package pro.respawn.flowmvi.sample.features.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.requireLifecycle
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.sample.arch.di.container
import pro.respawn.flowmvi.sample.features.home.HomeState.DisplayingHome
import pro.respawn.flowmvi.sample.navigation.AppNavigator
import pro.respawn.flowmvi.sample.ui.widgets.RErrorView
import pro.respawn.flowmvi.sample.ui.widgets.RScaffold
import pro.respawn.flowmvi.sample.ui.widgets.TypeCrossfade

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigator: AppNavigator,
) = with(container<HomeContainer, _, _, _>()) {

    val state by subscribe(requireLifecycle()) {

    }

    RScaffold {
        HomeScreenContent(state)
    }
}

@Composable
private fun IntentReceiver<HomeIntent>.HomeScreenContent(
    state: HomeState,
) = TypeCrossfade(state) {
    when (this) {
        is HomeState.Error -> RErrorView(e)
        is HomeState.Loading -> CircularProgressIndicator()
        is DisplayingHome -> Column {

        }
    }
}
