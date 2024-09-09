package pro.respawn.flowmvi.debugger.server.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.PopupProperties

@Composable
internal fun rememberDropDownActions(
    vararg key: Any,
    builder: () -> List<DropDownActions.Action>
) = remember(keys = key) { DropDownActions(builder()) }

@Immutable
internal data class DropDownActions(
    val actions: List<Action>,
) {

    @Immutable
    data class Action(
        val text: String,
        val tint: Color? = null,
        val icon: ImageVector? = null,
        val iconTint: Color = Color.Unspecified,
        val badged: Boolean = false,
        val onClick: () -> Unit,
    )
}

/**
 * Will be hidden when actions are empty
 */
@Composable
internal fun RDropDownMenu(
    expanded: Boolean,
    onExpand: () -> Unit,
    actions: DropDownActions,
    modifier: Modifier = Modifier,
    button: @Composable () -> Unit,
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
                    leadingIcon = icon@{
                        Icon(action.icon ?: return@icon, contentDescription = null, tint = action.iconTint)
                    },
                    onClick = {
                        action.onClick()
                        onExpand()
                    },
                )
            }
        }
    }
}
