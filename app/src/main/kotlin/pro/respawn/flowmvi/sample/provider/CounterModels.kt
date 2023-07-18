package pro.respawn.flowmvi.sample.provider

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

sealed interface CounterState : MVIState {
    object Loading : CounterState
    data class Error(val e: Exception) : CounterState
    data class DisplayingCounter(
        val timer: Int,
        val counter: Int = 0,
        val param: String,
    ) : CounterState
}

sealed interface CounterIntent : MVIIntent {
    object ClickedCounter : CounterIntent
}

sealed interface CounterAction : MVIAction {
    data class ShowSnackbar(val res: Int) : CounterAction
}
