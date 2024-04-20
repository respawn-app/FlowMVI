package pro.respawn.flowmvi.sample.features.logging

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.DefaultLifecycle
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.sample.Res
import pro.respawn.flowmvi.sample.arch.di.container
import pro.respawn.flowmvi.sample.features.logging.LoggingAction.SentLog
import pro.respawn.flowmvi.sample.features.logging.LoggingIntent.ClickedSendLog
import pro.respawn.flowmvi.sample.features.logging.LoggingState.DisplayingLogs
import pro.respawn.flowmvi.sample.logging_feature_title
import pro.respawn.flowmvi.sample.navigation.util.Navigator
import pro.respawn.flowmvi.sample.navigation.util.backNavigator
import pro.respawn.flowmvi.sample.ui.widgets.CodeText
import pro.respawn.flowmvi.sample.ui.widgets.RErrorView
import pro.respawn.flowmvi.sample.ui.widgets.RFilledButton
import pro.respawn.flowmvi.sample.ui.widgets.RScaffold
import pro.respawn.flowmvi.sample.ui.widgets.TypeCrossfade
import pro.respawn.flowmvi.sample.util.adaptiveWidth
import pro.respawn.flowmvi.sample.util.formatAsMultiline
import pro.respawn.flowmvi.sample.util.verticalListPaddings

private const val Description = """
    FlowMVI provides a multiplatform logging setup out of the box. 
    You can see everything that happens in the store in your device's console or customize the logging to print to any
    source you wish. 
    \n\n
    For example, the code below sends logs back to the store to display on-screen. 
"""

//language=kotlin
private const val Code = """
object InMemoryLogger : StoreLogger {

    val logs = MutableStateFlow<List<String>>(emptyList())

    override fun log(level: StoreLogLevel, tag: String?, message: () -> String) {
        logs.update { (it + "{level.asSymbol} {tag.orEmpty()}: {message()}") }
    }
}

internal class LoggingContainer(
    configuration: StoreConfiguration,
) : Container<LoggingState, LoggingIntent, LoggingAction> {

    override val store = store(LoggingState.Loading) {
        name = "LoggingStore"
        logger = InMemoryLogger
        enableLogging()
        
        whileSubscribed {
            InMemoryLogger.logs.onEach {
                updateState {
                    LoggingState.DisplayingLogs(it)
                }
            }.consume(Dispatchers.Default)
        }
    }
}
"""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggingScreen(
    navigator: Navigator,
) = with(container<LoggingContainer, _, _, _>()) {
    val listState = rememberLazyListState()

    val state by subscribe(DefaultLifecycle) {
        when (it) {
            is SentLog -> listState.animateScrollToItem(it.logsSize)
        }
    }

    RScaffold(
        title = stringResource(Res.string.logging_feature_title),
        onBack = navigator.backNavigator,
    ) {
        LoggingScreenContent(state, listState)
    }
}

@Composable
private fun IntentReceiver<LoggingIntent>.LoggingScreenContent(
    state: LoggingState,
    listState: LazyListState,
) = TypeCrossfade(state) {
    when (this) {
        is LoggingState.Loading -> CircularProgressIndicator()
        is LoggingState.Error -> RErrorView(e)
        is DisplayingLogs -> LazyColumn(
            modifier = Modifier.fillMaxHeight().adaptiveWidth(),
            contentPadding = WindowInsets.verticalListPaddings(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp),
                ) {
                    Text(Description.formatAsMultiline())
                    Spacer(Modifier.height(12.dp))
                    CodeText(Code)
                    Spacer(Modifier.height(12.dp))
                    RFilledButton(
                        onClick = { intent(ClickedSendLog) },
                        content = { Text("Send logs") },
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
            items(logs) {
                Text(
                    text = it,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}
