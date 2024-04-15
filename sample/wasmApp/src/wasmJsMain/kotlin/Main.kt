@file:Suppress("MissingPackageDeclaration")
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import pro.respawn.flowmvi.sample.navigation.AppContent
import pro.respawn.flowmvi.sample.navigation.component.RootComponent
import pro.respawn.flowmvi.sample.platform.NoOpPlatformFeatureLauncher
import org.w3c.dom.Document
import pro.respawn.flowmvi.sample.util.Json

private const val KEY_SAVED_STATE = "saved_state"

internal fun SerializableContainer.encodeToString(): String =
    Json.encodeToString(SerializableContainer.serializer(), this)

internal fun String.decodeSerializableContainer(): SerializableContainer? = try {
    Json.decodeFromString(SerializableContainer.serializer(), this)
} catch (expected: Exception) {
    null
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val registry = LifecycleRegistry()
    val stateKeeper = StateKeeperDispatcher(
        savedState = localStorage.getItem(KEY_SAVED_STATE)?.decodeSerializableContainer()
    )
    val root = RootComponent(
        androidFeatures = NoOpPlatformFeatureLauncher,
        context = DefaultComponentContext(
            lifecycle = registry,
            stateKeeper = stateKeeper,
        ),
    )
    registry.attachToDocument()
    window.onbeforeunload = {
        localStorage.setItem(KEY_SAVED_STATE, stateKeeper.save().encodeToString())
        null
    }
    CanvasBasedWindow(
        canvasElementId = "ComposeTarget",
        title = "FlowMVI",
    ) { AppContent(root) }
}

private fun LifecycleRegistry.attachToDocument() {
    fun onVisibilityChanged() {
        if (visibilityState(document) == "visible") {
            resume()
        } else {
            stop()
        }
    }

    onVisibilityChanged()

    document.addEventListener(type = "visibilitychange", callback = { onVisibilityChanged() })
}

// Workaround for Document#visibilityState not available in Wasm
@JsFun("(document) => document.visibilityState")
private external fun visibilityState(document: Document): String
