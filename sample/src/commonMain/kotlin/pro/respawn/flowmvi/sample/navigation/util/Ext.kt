package pro.respawn.flowmvi.sample.navigation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

typealias BackHandler = (() -> Unit)?

val Navigator.backNavigator: BackHandler
    @Composable get() {
        val hasBack by rememberBackNavigationState()
        return if (hasBack) ::back else null
    }
