package pro.respawn.flowmvi.sample

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.LambdaIntent

@Immutable
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

@Immutable
sealed interface CounterIntent : MVIIntent {

    data object ClickedCounter : CounterIntent
    data object ClickedUndo : CounterIntent
}

@Immutable
sealed interface CounterAction : MVIAction {

    data object ShowErrorMessage : CounterAction
    data object ShowLambdaMessage : CounterAction
}
