package pro.respawn.flowmvi.sample.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import pro.respawn.flowmvi.sample.ui.theme.MVITheme

class ComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        setContent {
            MVITheme {
                ComposeScreen()
            }
        }
    }
}
