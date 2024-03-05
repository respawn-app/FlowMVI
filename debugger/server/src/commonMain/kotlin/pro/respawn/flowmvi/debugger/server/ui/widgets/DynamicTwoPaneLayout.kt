package pro.respawn.flowmvi.debugger.server.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun DynamicTwoPaneLayout(
    secondPaneVisible: Boolean,
    firstPaneContent: @Composable () -> Unit,
    secondaryPaneContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.weight(1f).fillMaxHeight().animateContentSize()
        ) {
            firstPaneContent()
        }
        AnimatedVisibility(secondPaneVisible) {
            VerticalDivider(Modifier.padding(horizontal = 12.dp))
        }
        AnimatedVisibility(
            visible = secondPaneVisible,
            modifier = Modifier.weight(1f, false).fillMaxHeight().animateContentSize(),
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxSize(),
            ) {
                secondaryPaneContent()
            }
        }
    }
}
