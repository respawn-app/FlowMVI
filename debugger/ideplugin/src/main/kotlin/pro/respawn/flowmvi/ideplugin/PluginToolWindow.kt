package pro.respawn.flowmvi.ideplugin

import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposePanel
import com.arkivanov.decompose.DefaultComponentContext
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import pro.respawn.flowmvi.debugger.server.di.koin
import pro.respawn.flowmvi.debugger.server.navigation.AppContent
import pro.respawn.flowmvi.debugger.server.navigation.component.RootComponent
import pro.respawn.flowmvi.ideplugin.ui.PluginTheme

class PluginToolWindow : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        System.setProperty("compose.swing.render.on.graphics", "true")
        koin.createEagerInstances()

        val component = RootComponent(
            context = DefaultComponentContext(
                lifecycle = GlobalToolWindowListener.lifecycle
            )
        )
        toolWindow.apply {
            addComposePanel {
                PluginTheme { AppContent(component) }
            }
        }
    }

    companion object {

        // value derived from the plugin.xml and MUST be kept in sync
        const val Id = "FlowMVI"
    }
}

private fun ToolWindow.addComposePanel(
    height: Int = 800,
    width: Int = 800,
    y: Int = 0,
    x: Int = 0,
    displayName: String = "",
    isLockable: Boolean = true,
    content: @Composable ComposePanel.() -> Unit,
) = ComposePanel().apply {
    setBounds(x = x, y = y, width = width, height = height)
    setContent { content() }
}.also { contentManager.addContent(contentManager.factory.createContent(it, displayName, isLockable)) }
