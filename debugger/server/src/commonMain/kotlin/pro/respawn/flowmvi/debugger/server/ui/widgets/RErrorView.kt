package pro.respawn.flowmvi.debugger.server.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun RErrorView(
    e: Exception,
    modifier: Modifier = Modifier
) = Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    Text("An error has occurred", fontSize = 32.sp)
    SelectionContainer {
        Text("Message: ${e.message}")
        Text("stack trace: ${e.stackTraceToString()}")
        // TODO: Report to github link
    }
}
