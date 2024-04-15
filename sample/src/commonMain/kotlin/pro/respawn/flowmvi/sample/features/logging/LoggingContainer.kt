package pro.respawn.flowmvi.sample.features.logging

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.withState
import pro.respawn.flowmvi.logging.StoreLogLevel
import pro.respawn.flowmvi.logging.StoreLogger
import pro.respawn.flowmvi.logging.asSymbol
import pro.respawn.flowmvi.plugins.enableLogging
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileSubscribed
import pro.respawn.flowmvi.sample.features.logging.LoggingAction.SentLog
import pro.respawn.flowmvi.sample.features.logging.LoggingIntent.ClickedSendLog
import pro.respawn.flowmvi.sample.features.logging.LoggingState.DisplayingLogs

object InMemoryLogger : StoreLogger {

    val logs = MutableStateFlow<List<String>>(emptyList())
    private const val MaxLogs = 50

    override fun log(level: StoreLogLevel, tag: String?, message: () -> String) {
        // don't display state changes to avoid infinite loops
        if (level == StoreLogLevel.Trace) return
        logs.update { (it + "${level.asSymbol} ${tag.orEmpty()}: ${message()}").take(MaxLogs) }
    }
}

internal class LoggingContainer : Container<LoggingState, LoggingIntent, LoggingAction> {

    override val store = store(LoggingState.Loading) {
        name = "LoggingStore"
        logger = InMemoryLogger
        enableLogging()

        recover {
            updateState { LoggingState.Error(it) }
            null
        }

        whileSubscribed {
            InMemoryLogger.logs.onEach {
                updateState {
                    DisplayingLogs(it)
                }
            }.consume(Dispatchers.Default)
        }

        reduce { intent ->
            when (intent) {
                ClickedSendLog -> withState<DisplayingLogs, _> {
                    // one will be added by the action (hence index + 1)
                    action(SentLog(logs.size))
                }
            }
        }
    }
}
