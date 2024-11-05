package pro.respawn.flowmvi.ideplugin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import pro.respawn.flowmvi.debugger.server.ui.theme.RespawnTheme
import pro.respawn.flowmvi.debugger.server.ui.theme.rememberColorScheme

@Composable
fun PluginTheme(content: @Composable () -> Unit) {
    val ideTheme = rememberIntelliJTheme()
    val colorTheme = rememberColorScheme(dark = ideTheme.isDark)
    val colors = remember(ideTheme, colorTheme) {
        colorTheme.copy(
            primary = ideTheme.primary,
            background = ideTheme.background,
            onBackground = ideTheme.onBackground,
            surface = ideTheme.surface,
            onSurface = ideTheme.onSurface,
            surfaceContainer = ideTheme.surface,
            surfaceContainerLowest = ideTheme.surface,
            surfaceContainerLow = ideTheme.surface,
            surfaceContainerHigh = ideTheme.surface,
            surfaceContainerHighest = ideTheme.surface,
            surfaceVariant = ideTheme.surface,
            onSurfaceVariant = ideTheme.onSurface,
            onPrimary = ideTheme.onPrimary,
        )
    }
    RespawnTheme(
        colors = colors,
        content = content
    )
}
