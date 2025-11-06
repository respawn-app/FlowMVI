package pro.respawn.flowmvi.sample.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.Subject
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SyncLock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.sample.BuildFlags
import pro.respawn.flowmvi.sample.Res
import pro.respawn.flowmvi.sample.app_name
import pro.respawn.flowmvi.sample.arch.di.container
import pro.respawn.flowmvi.sample.decompose_feature_title
import pro.respawn.flowmvi.sample.di_feature_title
import pro.respawn.flowmvi.sample.features.home.HomeAction.GoToFeature
import pro.respawn.flowmvi.sample.features.home.HomeFeature.Decompose
import pro.respawn.flowmvi.sample.features.home.HomeFeature.DiConfig
import pro.respawn.flowmvi.sample.features.home.HomeFeature.LCE
import pro.respawn.flowmvi.sample.features.home.HomeFeature.Logging
import pro.respawn.flowmvi.sample.features.home.HomeFeature.Progressive
import pro.respawn.flowmvi.sample.features.home.HomeFeature.SST
import pro.respawn.flowmvi.sample.features.home.HomeFeature.SavedState
import pro.respawn.flowmvi.sample.features.home.HomeFeature.Simple
import pro.respawn.flowmvi.sample.features.home.HomeFeature.UndoRedo
import pro.respawn.flowmvi.sample.features.home.HomeFeature.XmlViews
import pro.respawn.flowmvi.sample.features.home.HomeIntent.ClickedFeature
import pro.respawn.flowmvi.sample.features.home.HomeState.DisplayingHome
import pro.respawn.flowmvi.sample.ic_flowmvi_32
import pro.respawn.flowmvi.sample.lce_feature_title
import pro.respawn.flowmvi.sample.logging_feature_title
import pro.respawn.flowmvi.sample.navigation.AppNavigator
import pro.respawn.flowmvi.sample.navigation.util.backNavigator
import pro.respawn.flowmvi.sample.platform_feature_unavailable_label
import pro.respawn.flowmvi.sample.progressive_feature_title
import pro.respawn.flowmvi.sample.savedstate_feature_title
import pro.respawn.flowmvi.sample.simple_feature_title
import pro.respawn.flowmvi.sample.sst_feature_title
import pro.respawn.flowmvi.sample.ui.theme.rainbow
import pro.respawn.flowmvi.sample.ui.widgets.RErrorView
import pro.respawn.flowmvi.sample.ui.widgets.RIcon
import pro.respawn.flowmvi.sample.ui.widgets.RMenuItem
import pro.respawn.flowmvi.sample.ui.widgets.RScaffold
import pro.respawn.flowmvi.sample.ui.widgets.TypeCrossfade
import pro.respawn.flowmvi.sample.undoredo_feature_title
import pro.respawn.flowmvi.sample.util.Platform
import pro.respawn.flowmvi.sample.util.platform
import pro.respawn.flowmvi.sample.xml_feature_title
import pro.respawn.kmmutils.compose.resources.string

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigator: AppNavigator,
) = with(container<HomeContainer, _, _, _>()) {
    val state by subscribe { action ->
        when (action) {
            is HomeAction.GoToInfo -> navigator.info()
            is GoToFeature -> when (action.feature) {
                Simple -> navigator.simpleFeature()
                LCE -> navigator.lceFeature()
                SavedState -> navigator.savedStateFeature()
                DiConfig -> navigator.diConfigFeature()
                Logging -> navigator.loggingFeature()
                XmlViews -> navigator.xmlActivity()
                UndoRedo -> navigator.undoRedoFeature()
                Decompose -> navigator.decomposeFeature()
                Progressive -> navigator.progressiveFeature()
                SST -> navigator.stateTransactionsFeature()
            }
        }
    }

    RScaffold(
        title = stringResource(Res.string.app_name),
        onBack = navigator.backNavigator,
        actions = { HomeActions() },
    ) {
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
            modifier = Modifier.fillMaxHeight()
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_flowmvi_32),
                contentDescription = null,
                modifier = Modifier.padding(vertical = 48.dp).size(108.dp),
                tint = Color.Unspecified,
            )
            Text(
                text = BuildFlags.ProjectDescription,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp).widthIn(max = 400.dp)
            )
            Spacer(Modifier.height(12.dp))
            HomeFeature.entries.forEachIndexed { i, item ->
                RMenuItem(
                    enabled = item.enabled,
                    title = stringResource(item.title),
                    color = rainbow[i % rainbow.size],
                    icon = item.icon,
                    subtitle = item.subtitle?.string(),
                    onClick = { intent(ClickedFeature(item)) }
                )
            }
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun IntentReceiver<HomeIntent>.HomeActions() {
    if (BuildFlags.platform == Platform.Android || BuildFlags.platform == Platform.Apple) RIcon(
        icon = Icons.Rounded.Info,
        onClick = { intent(HomeIntent.ClickedInfo) }
    )
}

private val HomeFeature.title
    get() = when (this) {
        Simple -> Res.string.simple_feature_title
        LCE -> Res.string.lce_feature_title
        SavedState -> Res.string.savedstate_feature_title
        DiConfig -> Res.string.di_feature_title
        Logging -> Res.string.logging_feature_title
        XmlViews -> Res.string.xml_feature_title
        UndoRedo -> Res.string.undoredo_feature_title
        Decompose -> Res.string.decompose_feature_title
        Progressive -> Res.string.progressive_feature_title
        SST -> Res.string.sst_feature_title
    }

private val HomeFeature.icon
    get() = when (this) {
        Simple -> Icons.AutoMirrored.Rounded.Help
        LCE -> Icons.Rounded.Refresh
        SavedState -> Icons.Rounded.Save
        DiConfig -> Icons.Rounded.Download
        Logging -> Icons.AutoMirrored.Rounded.Subject
        XmlViews -> Icons.Rounded.Code
        UndoRedo -> Icons.AutoMirrored.Rounded.Undo
        Decompose -> Icons.Rounded.AccountTree
        Progressive -> Icons.Rounded.Layers
        SST -> Icons.Rounded.SyncLock
    }

private val HomeFeature.enabled get() = platform == null || BuildFlags.platform == platform

private val HomeFeature.subtitle get() = Res.string.platform_feature_unavailable_label.takeIf { !enabled }
