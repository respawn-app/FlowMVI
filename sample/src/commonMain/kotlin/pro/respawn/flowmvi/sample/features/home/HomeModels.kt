package pro.respawn.flowmvi.sample.features.home

import androidx.compose.runtime.Immutable
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import kotlin.jvm.JvmInline

enum class HomeFeature {
    Simple, LCE, SavedState
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
}

@Immutable
internal sealed interface HomeAction : MVIAction {

    data class GoToFeature(val feature: HomeFeature) : HomeAction
}