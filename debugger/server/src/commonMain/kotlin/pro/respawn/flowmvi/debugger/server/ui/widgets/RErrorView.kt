package pro.respawn.flowmvi.debugger.server.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.sp
import pro.respawn.flowmvi.debugger.server.BuildFlags
import pro.respawn.kmmutils.compose.annotate

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
        Column {
            Text("Message: ${e.message}")
            Text("stack trace: ${e.stackTraceToString()}", fontFamily = FontFamily.Monospace)
            Text(
                textDecoration = TextDecoration.Underline,
                text = "Please report this to Github".annotate {
                    withLink(LinkAnnotation.Url(BuildFlags.ProjectUrl)) { append(it) }
                }
            )
        }
    }
}
