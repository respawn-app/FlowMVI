package pro.respawn.flowmvi.sample.features.logging

import androidx.compose.runtime.Immutable
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@Immutable
internal sealed interface LoggingState : MVIState {

    data object Loading : LoggingState
    data class Error(val e: Exception?) : LoggingState
    data class DisplayingLogs(val logs: List<String>) : LoggingState {

        override fun toString(): String = "DisplayingLogs(size = ${logs.size})"
    }
}

@Immutable
internal sealed interface LoggingIntent : MVIIntent {

    data object ClickedSendLog : LoggingIntent
}

@Immutable
internal sealed interface LoggingAction : MVIAction {

    data class SentLog(val logsSize: Int) : LoggingAction
}
