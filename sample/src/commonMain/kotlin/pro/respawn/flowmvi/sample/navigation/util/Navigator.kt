package pro.respawn.flowmvi.sample.navigation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State

@Stable
interface Navigator {

    fun back()

    @Suppress("TopLevelComposableFunctions")
    @Composable
    fun rememberBackNavigationState(): State<Boolean>
}
