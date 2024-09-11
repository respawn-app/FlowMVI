package pro.respawn.flowmvi.debugger.server.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.PopupProperties
import pro.respawn.flowmvi.debugger.server.ui.theme.Size

@Composable
fun rememberDropDownMenuState(isVisible: Boolean = false) = remember { DropDownMenuState(isVisible) }

class DropDownMenuState internal constructor(
    isVisible: Boolean = false,
) {

    var visible by mutableStateOf(isVisible)
        private set

    fun show() {
        visible = true
    }

    fun hide() {
        visible = false
    }

    fun toggle() {
        visible = !visible
    }
}

/**
 * Will be hidden when actions are empty
 */
@Composable
fun RDropDownMenu(
    button: @Composable DropDownMenuState.() -> Unit,
    modifier: Modifier = Modifier,
    state: DropDownMenuState = rememberDropDownMenuState(),
    actions: @Composable DropDownMenuState.() -> Unit,
) {
    // box needed for popup anchoring
    Box(modifier = modifier) {
        state.button()
        DropdownMenu(
            expanded = state.visible,
            onDismissRequest = { state.hide() },
            properties = remember { PopupProperties() },
            shape = MaterialTheme.shapes.medium,
        ) { state.actions() }
    }
}

@Composable
fun DropDownMenuState.DropDownAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    hasBadge: Boolean = false,
    tint: Color = LocalContentColor.current,
) = DropdownMenuItem(
    modifier = modifier,
    text = { Text(text = text) },
    leadingIcon = icon?.let {
        {
            BadgedBox(
                badge = badge@{
                    AnimatedVisibility(hasBadge) {
                        Dot(color = MaterialTheme.colorScheme.error)
                    }
                },
            ) {
                RIcon(
                    icon = it,
                    color = tint,
                    size = Size.smallIcon
                )
            }
        }
    },
    onClick = {
        hide()
        onClick()
    },
)
