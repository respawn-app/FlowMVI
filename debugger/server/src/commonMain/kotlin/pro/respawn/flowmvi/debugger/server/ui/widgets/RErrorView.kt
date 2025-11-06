package pro.respawn.flowmvi.debugger.server.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pro.respawn.flowmvi.debugger.server.DebuggerDefaults.ReportIssueUrl
import pro.respawn.flowmvi.debugger.server.ui.util.setText
import pro.respawn.kmmutils.compose.annotate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RErrorView(
    e: Exception,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) = Column(
    modifier = modifier
        .widthIn(max = 600.dp)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    val clipboard = LocalClipboard.current
    val handler = LocalUriHandler.current
    Text("An error has occurred", style = MaterialTheme.typography.headlineMedium)
    Text(text = "Message: ${e.message}")
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        OutlinedButton(
            onClick = { clipboard.setText(e.stackTraceToString()) },
        ) { Text("Copy stack trace") }
        AnimatedVisibility(onRetry != null) {
            Button(
                onClick = { onRetry?.invoke() },
            ) { Text("Retry") }
        }
        OutlinedButton(
            onClick = { handler.openUri(ReportIssueUrl) },
        ) { Text("Report on Github") }
    }
    SelectionContainer {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = e.stackTraceToString(),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            textAlign = TextAlign.Start,
        )
    }
}
