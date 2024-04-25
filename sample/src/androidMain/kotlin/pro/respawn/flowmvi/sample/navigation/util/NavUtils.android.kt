package pro.respawn.flowmvi.sample.navigation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

actual val LocalWindowSize: DpSize
    @Composable get() = LocalConfiguration.current.run {
        DpSize(screenWidthDp.dp, screenHeightDp.dp)
    }
