package pro.respawn.flowmvi.debugger.server.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.window.PopupProperties

@Composable
fun rememberDropDownActions(vararg key: Any, builder: () -> List<DropDownActions.Action>) = remember(keys = key) {
    DropDownActions(builder())
}

@Immutable
data class DropDownActions(
    val actions: List<Action>,
) {

    @Immutable
    data class Action(
        val text: String,
        val tint: Color? = null,
        val badged: Boolean = false,
        val onClick: () -> Unit,
    )
}

/**
 * Will be hidden when actions are empty
 */
@Composable
fun RDropDownMenu(
    expanded: Boolean,
    onExpand: () -> Unit,
    button: @Composable () -> Unit,
    actions: DropDownActions,
    modifier: Modifier = Modifier,
) {
    // box needed for popup anchoring
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = actions.actions.isNotEmpty(),
            modifier = Modifier.minimumInteractiveComponentSize(),
            content = { button() },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onExpand,
            properties = remember { PopupProperties() },
        ) {
            actions.actions.forEach { action ->
                DropdownMenuItem(
                    text = { Text(text = action.text) },
                    onClick = {
                        action.onClick()
                        onExpand()
                    },
                )
            }
        }
    }
}
