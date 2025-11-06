package pro.respawn.flowmvi.sample.features.home

import androidx.compose.runtime.Immutable
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.sample.util.Platform
import kotlin.jvm.JvmInline

enum class HomeFeature(val platform: Platform? = null) {
    Simple, LCE, SavedState, DiConfig, Progressive, Logging, SST, UndoRedo, Decompose, XmlViews(Platform.Android)
}

@Immutable
internal sealed interface HomeState : MVIState {

    data object Loading : HomeState
    data class Error(val e: Exception?) : HomeState
    data object DisplayingHome : HomeState
}

@Immutable
internal sealed interface HomeIntent : MVIIntent {

    @JvmInline
    value class ClickedFeature(val value: HomeFeature) : HomeIntent

    data object ClickedInfo : HomeIntent
}

@Immutable
internal sealed interface HomeAction : MVIAction {

    data class GoToFeature(val feature: HomeFeature) : HomeAction
    data object GoToInfo : HomeAction
}
