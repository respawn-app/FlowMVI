package pro.respawn.flowmvi.debugger.app

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import org.jetbrains.compose.resources.painterResource
import pro.respawn.flowmvi.debugger.server.di.koin
import pro.respawn.flowmvi.debugger.server.navigation.AppContent
import pro.respawn.flowmvi.debugger.server.navigation.component.RootComponent
import pro.respawn.flowmvi.server.generated.resources.icon_nobg_32
import pro.respawn.flowmvi.server.generated.resources.Res as UiR

fun main() = application {
    val state = rememberWindowState(
        width = 1200.dp,
        height = 800.dp,
        position = WindowPosition.Aligned(Alignment.Center),
    )
    val lifecycle = LifecycleRegistry()

    LifecycleController(lifecycle, state)

    koin.createEagerInstances()

    val component = RootComponent(
        context = DefaultComponentContext(
            lifecycle = lifecycle,
        )
    )
    Window(
        onCloseRequest = ::exitApplication,
        icon = painterResource(UiR.drawable.icon_nobg_32),
        title = "FlowMVI Debugger",
        state = state,
    ) { AppContent(component) }
}
