package pro.respawn.flowmvi.sample.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.sample.Res
import pro.respawn.flowmvi.sample.generic_error_message
import pro.respawn.flowmvi.sample.retry
import pro.respawn.flowmvi.sample.ui.icons.Icons
import pro.respawn.flowmvi.sample.ui.icons.Warning

@Composable
fun RErrorView(
    e: Exception?,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier,
    verticalArrangement = Arrangement.Center,
) {
    RIcon(Icons.Warning, size = 48.dp)
    Spacer(Modifier.height(48.dp))
    Text(
        text = e?.message ?: stringResource(Res.string.generic_error_message),
    )
    Spacer(Modifier.height(24.dp))
    AnimatedVisibility(onRetry != null) {
        ROutlinedButton(
            onClick = onRetry ?: {},
        ) {
            Text(stringResource(Res.string.retry))
        }
    }
}
