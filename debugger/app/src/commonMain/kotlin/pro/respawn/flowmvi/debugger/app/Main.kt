package pro.respawn.flowmvi.debugger.app

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.KoinContext
import pro.respawn.flowmvi.compose.dsl.ProvideSubscriberLifecycle
import pro.respawn.flowmvi.debugger.app.di.koin
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineScreen
import pro.respawn.flowmvi.debugger.server.ui.theme.Resources
import pro.respawn.flowmvi.debugger.server.ui.theme.RespawnTheme
import pro.respawn.flowmvi.essenty.compose.asSubscriberLifecycle

@OptIn(ExperimentalResourceApi::class)
fun main() {
    val registry = LifecycleRegistry()
    application {
        val state = rememberWindowState(
            width = 1200.dp,
            height = 800.dp,
            position = WindowPosition.Aligned(Alignment.Center),
        )
        Window(
            onCloseRequest = ::exitApplication,
            icon = painterResource(Resources.projectIcon),
            title = "FlowMVI Debugger",
            state = state,
        ) {
            LifecycleController(registry, state)
            KoinContext(koin) {
                RespawnTheme {
                    ProvideSubscriberLifecycle(registry.asSubscriberLifecycle) {
                        TimelineScreen()
                    }
                }
            }
        }
    }
}
