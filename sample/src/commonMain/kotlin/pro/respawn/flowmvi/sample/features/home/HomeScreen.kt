package pro.respawn.flowmvi.sample.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Vaccines
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.requireLifecycle
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.sample.arch.di.container
import pro.respawn.flowmvi.sample.features.home.HomeAction.GoToFeature
import pro.respawn.flowmvi.sample.features.home.HomeFeature.DiConfig
import pro.respawn.flowmvi.sample.features.home.HomeFeature.LCE
import pro.respawn.flowmvi.sample.features.home.HomeFeature.SavedState
import pro.respawn.flowmvi.sample.features.home.HomeFeature.Simple
import pro.respawn.flowmvi.sample.features.home.HomeIntent.ClickedFeature
import pro.respawn.flowmvi.sample.features.home.HomeState.DisplayingHome
import pro.respawn.flowmvi.sample.generated.resources.Res
import pro.respawn.flowmvi.sample.generated.resources.app_name
import pro.respawn.flowmvi.sample.generated.resources.di_feature_title
import pro.respawn.flowmvi.sample.generated.resources.lce_feature_title
import pro.respawn.flowmvi.sample.generated.resources.savedstate_feature_title
import pro.respawn.flowmvi.sample.generated.resources.simple_feature_title
import pro.respawn.flowmvi.sample.navigation.AppNavigator
import pro.respawn.flowmvi.sample.navigation.util.backNavigator
import pro.respawn.flowmvi.sample.ui.theme.rainbow
import pro.respawn.flowmvi.sample.ui.widgets.RErrorView
import pro.respawn.flowmvi.sample.ui.widgets.RMenuItem
import pro.respawn.flowmvi.sample.ui.widgets.RScaffold
import pro.respawn.flowmvi.sample.ui.widgets.TypeCrossfade

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigator: AppNavigator,
) = with(container<HomeContainer, _, _, _>()) {

    val state by subscribe(requireLifecycle()) { action ->
        when (action) {
            is GoToFeature -> when (action.feature) {
                Simple -> navigator.simpleFeature()
                LCE -> navigator.lceFeature()
                SavedState -> navigator.savedStateFeature()
                DiConfig -> navigator.diConfigFeature()
            }
        }
    }

    RScaffold(title = stringResource(Res.string.app_name), onBack = navigator.backNavigator) {
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
        is DisplayingHome -> Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
        ) {
            HomeFeature.entries.forEachIndexed { i, item ->
                RMenuItem(
                    title = stringResource(item.title),
                    color = rainbow[i % rainbow.size],
                    icon = item.icon,
                    onClick = { intent(ClickedFeature(item)) }
                )
            }
        }
    }
}

private val HomeFeature.title
    get() = when (this) {
        Simple -> Res.string.simple_feature_title
        LCE -> Res.string.lce_feature_title
        SavedState -> Res.string.savedstate_feature_title
        DiConfig -> Res.string.di_feature_title
    }

private val HomeFeature.icon
    get() = when (this) {
        Simple -> Icons.AutoMirrored.Rounded.Help
        LCE -> Icons.Rounded.Refresh
        SavedState -> Icons.Rounded.Save
        DiConfig -> Icons.Rounded.Download
    }
