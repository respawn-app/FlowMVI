import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.webhistory.DefaultWebHistoryController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import kotlinx.browser.document
import pro.respawn.flowmvi.sample.navigation.AppContent
import pro.respawn.flowmvi.sample.navigation.component.RootComponent
import pro.respawn.flowmvi.sample.platform.NoOpPlatformFeatureLauncher

@OptIn(ExperimentalComposeUiApi::class, ExperimentalDecomposeApi::class)
fun main() {
    val registry = LifecycleRegistry()
    val root = RootComponent(
        androidFeatures = NoOpPlatformFeatureLauncher,
        webHistoryController = DefaultWebHistoryController(),
        context = DefaultComponentContext(
            lifecycle = registry
        ),
    )
    registry.attachToDocument()
    CanvasBasedWindow(
        canvasElementId = "ComposeTarget",
        title = "FlowMVI",
    ) { AppContent(root) }
}

private fun LifecycleRegistry.attachToDocument() {
    fun onVisibilityChanged() {
        // if (document.visibilityState == "visible") {
        //     resume()
        // } else {
        //     stop()
        // }
    }

    // onVisibilityChanged()

    resume()

    document.addEventListener(type = "visibilitychange", callback = { onVisibilityChanged() })
}
