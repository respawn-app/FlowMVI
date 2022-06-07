package com.nek12.flowMVI.sample.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nek12.flowMVI.sample.ui.theme.MVITheme

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
