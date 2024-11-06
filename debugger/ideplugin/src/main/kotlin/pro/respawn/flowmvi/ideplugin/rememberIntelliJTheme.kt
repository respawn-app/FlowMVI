@file:Suppress("Filename")

package pro.respawn.flowmvi.ideplugin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.application.ApplicationManager
import javax.swing.UIManager
import java.awt.Color as AwtColor

@Composable
fun rememberIntelliJTheme(): IntelliJTheme {
    val themeExtractor = remember<IntelliJThemeExtractor> { IntelliJThemeExtractorImpl() }
    val messageBus = remember { ApplicationManager.getApplication().messageBus.connect() }

    DisposableEffect(messageBus) {
        messageBus.subscribe(
            LafManagerListener.TOPIC,
            ThemeChangeListener(themeExtractor::invalidate),
        )

        onDispose {
            messageBus.disconnect()
        }
    }

    return themeExtractor.theme
}

private class ThemeChangeListener(
    private val onChanged: () -> Unit,
) : LafManagerListener {

    override fun lookAndFeelChanged(source: LafManager) = onChanged()
}

data class IntelliJTheme(
    val mode: Mode,
    val primary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val onPrimary: Color,
) {

    val isDark = mode == Mode.DARK
    enum class Mode {
        LIGHT,
        DARK,
    }
}

private interface IntelliJThemeExtractor {
    val theme: IntelliJTheme

    fun invalidate()
}

private class IntelliJThemeExtractorImpl : IntelliJThemeExtractor {

    override var theme by mutableStateOf(buildIntelliJTheme())

    override fun invalidate() {
        theme = buildIntelliJTheme()
    }

    private fun buildIntelliJTheme() = IntelliJTheme(
        mode = getCurrentTheme(),
        primary = getColor(ColorKey.LinkFg),
        onPrimary = getColor(ColorKey.ButtonBg),
        background = getColor(ColorKey.PanelBg),
        onBackground = getColor(ColorKey.PanelFg),
        surface = getColor(ColorKey.EditorBg),
        onSurface = getColor(ColorKey.EditorFg),
    )

    @Suppress("UnstableApiUsage")
    private fun getCurrentTheme(): IntelliJTheme.Mode {
        val info = LafManager.getInstance().currentUIThemeLookAndFeel

        return when {
            info == null || info.isDark -> IntelliJTheme.Mode.DARK
            else -> IntelliJTheme.Mode.LIGHT
        }
    }

    private fun getColor(key: ColorKey): Color = requireNotNull(UIManager.getColor(key.key)?.toComposeColor()) {
        "Color not found: ${key.key}"
    }

    companion object {
        enum class ColorKey(val key: String) {
            ButtonBg("Button.background"),
            LinkFg("Link.activeForeground"),
            PanelBg("Panel.background"),
            PanelFg("Panel.foreground"),
            EditorBg("EditorPane.background"),
            EditorFg("EditorPane.foreground"),
        }

        private fun AwtColor.toComposeColor(): Color = Color(red, green, blue, alpha)
    }
}
