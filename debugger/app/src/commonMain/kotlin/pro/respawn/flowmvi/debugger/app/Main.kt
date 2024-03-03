package pro.respawn.flowmvi.debugger.app

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.koin.compose.KoinContext
import org.koin.core.context.KoinContext
import pro.respawn.flowmvi.debugger.app.di.koin
import pro.respawn.flowmvi.debugger.app.ui.theme.RespawnTheme
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineScreen

fun main() = application {
    val state = rememberWindowState(
        width = 1800.dp,
        height = 1200.dp,
        position = WindowPosition.Aligned(Alignment.Center)
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "FlowMVI Debugger",
        state = state
    ) {
        KoinContext(koin) {
            RespawnTheme { TimelineScreen() }
        }
    }
}
