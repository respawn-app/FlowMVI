package pro.respawn.flowmvi.ideplugin

import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.pause
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.openapi.wm.ex.ToolWindowManagerListener.ToolWindowManagerEventType.ActivateToolWindow
import com.intellij.openapi.wm.ex.ToolWindowManagerListener.ToolWindowManagerEventType.HideToolWindow
import com.intellij.openapi.wm.ex.ToolWindowManagerListener.ToolWindowManagerEventType.RegisterToolWindow
import com.intellij.openapi.wm.ex.ToolWindowManagerListener.ToolWindowManagerEventType.ShowToolWindow
import com.intellij.openapi.wm.ex.ToolWindowManagerListener.ToolWindowManagerEventType.UnregisterToolWindow

@Suppress("UNUSED_PARAMETER")
class GlobalToolWindowListener(project: Project) : ToolWindowManagerListener {

    override fun stateChanged(
        toolWindowManager: ToolWindowManager,
        changeType: ToolWindowManagerListener.ToolWindowManagerEventType
    ) {
        if (PluginToolWindow.Id !in toolWindowManager.toolWindowIds) return
        val toolWindow = toolWindowManager.getToolWindow(PluginToolWindow.Id) ?: return
        println("Tool window: $changeType, ${toolWindow.id}")
        when (changeType) {
            RegisterToolWindow -> lifecycle.create()
            UnregisterToolWindow -> lifecycle.destroy()
            HideToolWindow -> lifecycle.stop()
            ActivateToolWindow, ShowToolWindow -> when {
                !toolWindow.isVisible -> lifecycle.stop()
                toolWindow.isActive -> lifecycle.resume()
                !toolWindow.isActive -> lifecycle.pause()
                else -> Unit
            }
            else -> Unit
        }
    }

    companion object {

        val lifecycle by lazy { LifecycleRegistry() }
    }
}
