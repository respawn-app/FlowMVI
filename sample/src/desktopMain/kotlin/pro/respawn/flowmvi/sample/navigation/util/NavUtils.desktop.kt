package pro.respawn.flowmvi.sample.navigation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.toSize

@OptIn(ExperimentalComposeUiApi::class)
actual val LocalWindowSize
    @Composable get() = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.toSize().toDpSize()
    }
