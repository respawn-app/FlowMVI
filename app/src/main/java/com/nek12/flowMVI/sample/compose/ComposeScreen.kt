package com.nek12.flowMVI.sample.compose

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nek12.flowMVI.android.compose.EmptyScope
import com.nek12.flowMVI.android.compose.MVIComposable
import com.nek12.flowMVI.android.compose.MVIIntentScope
import com.nek12.flowMVI.sample.R.string
import com.nek12.flowMVI.sample.compose.ComposeAction.GoToBasicActivity
import com.nek12.flowMVI.sample.compose.ComposeAction.ShowSnackbar
import com.nek12.flowMVI.sample.compose.ComposeIntent.ClickedCounter
import com.nek12.flowMVI.sample.compose.ComposeIntent.ClickedToBasicActivity
import com.nek12.flowMVI.sample.compose.ComposeState.DisplayingContent
import com.nek12.flowMVI.sample.compose.ComposeState.Empty
import com.nek12.flowMVI.sample.compose.ComposeState.Loading
import com.nek12.flowMVI.sample.view.BasicActivity
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@Composable
fun ComposeScreen() = MVIComposable(getViewModel<BaseClassViewModel>()) { state ->

    // this -> MVIIntentScope with utility functions available

    val context = LocalContext.current // we can't use composable functions in consume()
    val scaffoldState = rememberScaffoldState()

    consume { action ->
        // This block is run in a new coroutine each time we consume a new actions.
        // You can run suspending (but not blocking) code here safely
        // consume() block will only be called when a new action is emitted (independent of recompositions)
        when (action) {
            is GoToBasicActivity -> context.startActivity(
                Intent(context, BasicActivity::class.java)
            )
            // snackbar suspends consume(), we do not want to block action consumption here
            // so we'll launch a new coroutine
            is ShowSnackbar -> launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = context.getString(action.res)
                )
            }
        }
    }

    Scaffold(Modifier.fillMaxSize(), scaffoldState = scaffoldState) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center
        ) {
            ComposeScreenContent(state)
        }
    }
}

@Composable
fun MVIIntentScope<ComposeIntent, ComposeAction>.ComposeScreenContent(state: ComposeState) {
    Box(
        Modifier.fillMaxSize(),
        Alignment.Center,
    ) {
        when (state) {
            is DisplayingContent -> {
                Column(
                    Modifier.fillMaxSize(),
                    Arrangement.SpaceAround,
                    Alignment.CenterHorizontally,
                ) {
                    Column {
                        Text(
                            stringResource(id = string.counter_text, state.counter),
                            Modifier.clickable { send(ClickedCounter) } // send() is available in MVIIntentScope
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            stringResource(id = string.timer_template, state.timer),
                        )
                    }

                    Button(onClick = { send(ClickedToBasicActivity) }) {
                        Text(stringResource(id = string.to_basic_activity))
                    }
                }
            }
            Loading -> {
                CircularProgressIndicator()
            }
            Empty -> {
                Text(stringResource(string.compose_screen_empty))
            }
        }
    }
}

@Composable
@Preview(name = "ComposeScreen", showSystemUi = true, showBackground = true)
private fun ComposeScreenPreview() {
    EmptyScope { // Use this helper function to preview functions that use MVIIntentScope
        ComposeScreenContent(state = DisplayingContent(15, 0))
    }
}
