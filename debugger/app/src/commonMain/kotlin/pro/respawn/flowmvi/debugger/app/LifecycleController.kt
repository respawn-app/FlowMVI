package pro.respawn.flowmvi.debugger.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.WindowState
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.pause
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun LifecycleController(lifecycleRegistry: LifecycleRegistry, windowState: WindowState) {
    val info = LocalWindowInfo.current
    LaunchedEffect(lifecycleRegistry, windowState) {
        snapshotFlow(windowState::isMinimized).onEach { isMinimized ->
            if (isMinimized) lifecycleRegistry.stop() else lifecycleRegistry.resume()
        }.launchIn(this)

        snapshotFlow(info::isWindowFocused).onEach { isFocused ->
            if (isFocused) lifecycleRegistry.resume() else lifecycleRegistry.pause()
        }.launchIn(this)
    }

    DisposableEffect(lifecycleRegistry) {
        lifecycleRegistry.create()
        onDispose(lifecycleRegistry::destroy)
    }
}
