package pro.respawn.flowmvi.sample.features.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pro.respawn.flowmvi.sample.BuildFlags
import pro.respawn.kmmutils.compose.annotate
import pro.respawn.kmmutils.compose.underline

@Composable
fun InfoScreen() = Column(
    modifier = Modifier.fillMaxHeight().widthIn(max = 600.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(text = "This app is a Respawn open source initiative project.", textAlign = TextAlign.Center)
    Spacer(Modifier.height(20.dp))
    Text(
        text = "You can access the general privacy policy at\n".annotate {
            append(it)
            pushLink(LinkAnnotation.Url(BuildFlags.PrivacyUrl))
            append(BuildFlags.PrivacyUrl.underline())
            pop()
        },
        textAlign = TextAlign.Center
    )
}
