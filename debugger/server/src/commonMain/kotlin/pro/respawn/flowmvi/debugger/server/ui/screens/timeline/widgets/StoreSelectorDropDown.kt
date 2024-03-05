package pro.respawn.flowmvi.debugger.server.ui.screens.timeline.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.StoreItem
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineFilters
import pro.respawn.flowmvi.debugger.server.ui.screens.timeline.TimelineIntent
import pro.respawn.flowmvi.debugger.server.ui.widgets.DropDownActions
import pro.respawn.flowmvi.debugger.server.ui.widgets.RDropDownMenu
import pro.respawn.flowmvi.debugger.server.ui.widgets.rememberDropDownActions

@Composable
internal fun IntentReceiver<TimelineIntent>.StoreSelectorDropDown(
    stores: ImmutableList<StoreItem>,
    filters: TimelineFilters,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = stores.isNotEmpty(), modifier = modifier) {
        var expanded by remember { mutableStateOf(false) }
        RDropDownMenu(
            expanded = expanded,
            onExpand = { expanded = !expanded },
            button = {
                OutlinedButton(onClick = { expanded = !expanded }) {
                    Text(filters.store?.name ?: "All stores")
                }
            },
            actions = rememberDropDownActions(stores) {
                buildList {
                    add(
                        DropDownActions.Action("All stores") {
                            intent(TimelineIntent.StoreFilterSelected(null))
                        }
                    )
                    stores.forEach {
                        add(
                            DropDownActions.Action(text = it.name) {
                                intent(TimelineIntent.StoreFilterSelected(it))
                            }
                        )
                    }
                }
            },
        )
    }
}
