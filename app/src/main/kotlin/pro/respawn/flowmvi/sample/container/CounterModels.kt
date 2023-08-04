package pro.respawn.flowmvi.sample.container

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.LambdaIntent

sealed interface CounterState : MVIState, Parcelable {
    @Parcelize
    data object Loading : CounterState

    @Parcelize
    data class Error(val e: Exception) : CounterState

    @Parcelize
    data class DisplayingCounter(
        val timer: Int,
        val counter: Int = 0,
        val param: String,
    ) : CounterState
}

typealias CounterLambdaIntent = LambdaIntent<CounterState, CounterAction>

sealed interface CounterIntent : MVIIntent {
    data object ClickedCounter : CounterIntent
}

sealed interface CounterAction : MVIAction {
    data class ShowSnackbar(val res: Int) : CounterAction
}
