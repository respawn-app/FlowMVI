package pro.respawn.flowmvi.debugger.server.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SignalWifi4Bar
import androidx.compose.material.icons.rounded.SignalWifiOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.StoreItem
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent.StoreSelected

@Composable
internal fun IntentReceiver<TimelineIntent>.StoreSelectorDropDown(
    stores: ImmutableList<StoreItem>,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = stores.isNotEmpty(), modifier = modifier) {
        val connectedColor = MaterialTheme.colorScheme.primary
        val errorColor = MaterialTheme.colorScheme.error

        RDropDownMenu(
            button = {
                OutlinedButton(onClick = ::toggle) {
                    Text("Select a store")
                }
            },
            actions = {
                stores.forEach {
                    DropDownAction(
                        text = it.name,
                        icon = if (it.isConnected) Icons.Rounded.SignalWifi4Bar else Icons.Rounded.SignalWifiOff,
                        tint = if (it.isConnected) connectedColor else errorColor,
                        onClick = { intent(StoreSelected(it)) },
                    )
                }
            },
        )
    }
}
