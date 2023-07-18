package pro.respawn.flowmvi.sample.compose

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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import pro.respawn.flowmvi.android.compose.ConsumerScope
import pro.respawn.flowmvi.android.compose.EmptyScope
import pro.respawn.flowmvi.android.compose.StateProvider
import pro.respawn.flowmvi.android.compose.composable.MVIComposable
import pro.respawn.flowmvi.android.compose.consume
import pro.respawn.flowmvi.sample.R
import pro.respawn.flowmvi.sample.compose.ComposeAction.GoToBasicActivity
import pro.respawn.flowmvi.sample.compose.ComposeAction.ShowSnackbar
import pro.respawn.flowmvi.sample.compose.ComposeIntent.ClickedCounter
import pro.respawn.flowmvi.sample.compose.ComposeIntent.ClickedToBasicActivity
import pro.respawn.flowmvi.sample.compose.ComposeState.DisplayingContent
import pro.respawn.flowmvi.sample.compose.ComposeState.Empty
import pro.respawn.flowmvi.sample.compose.ComposeState.Loading
import pro.respawn.flowmvi.sample.ui.theme.MVISampleTheme
import pro.respawn.flowmvi.sample.view.BasicActivity

private typealias Scope = ConsumerScope<ComposeState, ComposeIntent, ComposeAction>

@Composable
@Suppress("ComposableFunctionName")
fun ComposeScreen() = MVIComposable(getViewModel<BaseClassViewModel>()) {
    // this -> ConsumerScope with utility functions available

    val context = LocalContext.current // we can't use composable functions in consume()
    val scaffoldState = rememberScaffoldState()

    consume { action ->
        // This block is run in a new coroutine each time we consume a new actions.
        // You can run suspending (but not blocking) code here safely
        // consume() block will only be called when a new action is emitted (independent of recompositions)
        when (action) {
            is GoToBasicActivity -> context.startActivity(Intent(context, BasicActivity::class.java))
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
        ComposeScreenContent(state, modifier = Modifier.padding(it))
    }
}

@Composable
private fun Scope.ComposeScreenContent(
    state: ComposeState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is DisplayingContent -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.counter_text, state.counter),
                        // send() is available in ConsumerScope
                        modifier = Modifier.clickable { send(ClickedCounter) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(id = R.string.timer_template, state.timer),
                    )
                }

                Button(onClick = { send(ClickedToBasicActivity) }) {
                    Text(text = stringResource(id = R.string.navigate_basic_activity_button_label))
                }
            }
            is Loading -> CircularProgressIndicator()
            is Empty -> Text(text = stringResource(R.string.empty_message))
            is ComposeState.Error -> Text(stringResource(R.string.error_message))
        }
    }
}

private class PreviewProvider : StateProvider<ComposeState>(
    DisplayingContent(1, 1),
    Loading,
    Empty,
)

@Composable
@Preview(name = "ComposeScreen", showSystemUi = true, showBackground = true, backgroundColor = 0xFFFFFFFF)
private fun ComposeScreenPreview(
    @PreviewParameter(PreviewProvider::class) state: ComposeState,
) = MVISampleTheme {
    // Use this helper function to preview functions that use ConsumerScope
    EmptyScope {
        ComposeScreenContent(state = state)
    }
}
