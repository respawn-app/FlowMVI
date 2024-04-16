package pro.respawn.flowmvi.sample.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal actual fun rememberColorScheme(
    dark: Boolean,
    dynamic: Boolean,
) = remember(dark) { if (dark) DarkColors else LightColors }
