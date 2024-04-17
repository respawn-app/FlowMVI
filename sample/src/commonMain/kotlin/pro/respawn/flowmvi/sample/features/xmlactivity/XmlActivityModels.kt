package pro.respawn.flowmvi.sample.features.xmlactivity

import androidx.compose.runtime.Immutable
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
internal sealed interface XmlActivityState : MVIState {

    data object Loading : XmlActivityState
    data class Error(val e: Exception?) : XmlActivityState
    data class DisplayingCounter(val counter: Int) : XmlActivityState
}

@Immutable
internal sealed interface XmlActivityIntent : MVIIntent {

    data object ClickedIncrementCounter : XmlActivityIntent
}

@Immutable
internal sealed interface XmlActivityAction : MVIAction {

    data object ShowIncrementedSnackbar : XmlActivityAction
}
