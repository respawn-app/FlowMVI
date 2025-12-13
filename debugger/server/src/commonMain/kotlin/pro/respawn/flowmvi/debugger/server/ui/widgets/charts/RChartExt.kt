package pro.respawn.flowmvi.debugger.server.ui.widgets.charts

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun DefaultLabelText(
    value: String,
    style: TextStyle = MaterialTheme.typography.bodySmall,
) {
    Text(
        text = value,
        style = style,
        modifier = Modifier.padding(4.dp),
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis
    )
}

internal fun getValueLabels(labelAmount: Int, maxValue: Float, minValue: Float = 0f): List<Float> {
    val labelDelta = maxValue / labelAmount

    return (0..labelAmount).map {
        minValue + labelDelta * it
    }
}
