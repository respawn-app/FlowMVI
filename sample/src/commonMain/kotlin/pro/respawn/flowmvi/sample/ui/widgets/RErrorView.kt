package pro.respawn.flowmvi.sample.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.sample.generated.resources.Res
import pro.respawn.flowmvi.sample.generated.resources.generic_error_message

@Composable
fun RErrorView(
    e: Exception?,
    modifier: Modifier = Modifier
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier,
    verticalArrangement = Arrangement.Center,
) {
    RIcon(Icons.Rounded.Warning, modifier = Modifier.padding(bottom = 48.dp))
    Text(
        text = e?.message ?: stringResource(Res.string.generic_error_message),
    )
}
