package pro.respawn.flowmvi.sample

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.LambdaIntent

sealed interface CounterState : MVIState {

    data object Loading : CounterState

    data class Error(val e: Exception) : CounterState

    @Serializable
    @Parcelize
    data class DisplayingCounter(
        val timer: Int = 0,
        val counter: Int = 0,
        val input: String = "",
    ) : CounterState, Parcelable
}

typealias CounterLambdaIntent = LambdaIntent<CounterState, CounterAction>

sealed interface CounterIntent : MVIIntent {

    data object ClickedCounter : CounterIntent
    data object ClickedUndo : CounterIntent
    data object ClickedBack : CounterIntent

    @JvmInline
    value class InputChanged(val value: String) : CounterIntent
}

sealed interface CounterAction : MVIAction {

    data class ShowErrorMessage(val message: String?) : CounterAction
    data object ShowLambdaMessage : CounterAction
    data object GoBack : CounterAction
}
