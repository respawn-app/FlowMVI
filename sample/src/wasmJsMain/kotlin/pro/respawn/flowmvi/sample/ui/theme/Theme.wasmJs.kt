package pro.respawn.flowmvi.sample.ui.theme

import androidx.compose.runtime.Composable

@Composable
internal actual fun rememberColorScheme(
    dark: Boolean,
    dynamic: Boolean,
    // https://github.com/JetBrains/compose-multiplatform/issues/4637
) = DarkColors
