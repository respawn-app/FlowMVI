package pro.respawn.flowmvi.sample.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import pro.respawn.flowmvi.sample.compose.theme.MVISampleTheme

class ComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        setContent {
            MVISampleTheme {
                ComposeScreen { finish() }
            }
        }
    }
}
