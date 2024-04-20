package pro.respawn.flowmvi.sample

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinContext
import pro.respawn.flowmvi.sample.di.startKoin
import pro.respawn.flowmvi.sample.navigation.AppContent
import pro.respawn.flowmvi.sample.navigation.component.RootComponent

@OptIn(ExperimentalResourceApi::class, ExperimentalDecomposeApi::class)
fun main() = application {
    startKoin()
    val lifecycle = LifecycleRegistry()

    val state = rememberWindowState(
        width = 1200.dp,
        height = 800.dp,
        position = WindowPosition.Aligned(Alignment.Center),
    )
    LifecycleController(lifecycle, state)
    val component = RootComponent(
        context = DefaultComponentContext(
            lifecycle = lifecycle,
        )
    )
    Window(
        onCloseRequest = ::exitApplication,
        icon = painterResource(Res.drawable.ic_flowmvi_32),
        title = stringResource(Res.string.app_name),
        state = state,
    ) {
        KoinContext { AppContent(component) }
    }
}
