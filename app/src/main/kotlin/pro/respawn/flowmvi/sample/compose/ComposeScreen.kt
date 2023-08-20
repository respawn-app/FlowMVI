package pro.respawn.flowmvi.sample.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf
import pro.respawn.flowmvi.android.compose.ConsumerScope
import pro.respawn.flowmvi.android.compose.EmptyScope
import pro.respawn.flowmvi.android.compose.MVIComposable
import pro.respawn.flowmvi.android.compose.StateProvider
import pro.respawn.flowmvi.sample.CounterAction
import pro.respawn.flowmvi.sample.CounterAction.ShowErrorMessage
import pro.respawn.flowmvi.sample.CounterAction.ShowLambdaMessage
import pro.respawn.flowmvi.sample.CounterIntent
import pro.respawn.flowmvi.sample.CounterIntent.ClickedCounter
import pro.respawn.flowmvi.sample.CounterIntent.ClickedUndo
import pro.respawn.flowmvi.sample.CounterState
import pro.respawn.flowmvi.sample.CounterState.DisplayingCounter
import pro.respawn.flowmvi.sample.R
import pro.respawn.flowmvi.sample.compose.theme.MVISampleTheme
import pro.respawn.flowmvi.sample.di.storeViewModel

private typealias Scope = ConsumerScope<CounterIntent, CounterAction>

context(CoroutineScope)
fun ScaffoldState.snackbar(text: String) = launch { snackbarHostState.showSnackbar(text) }

@Composable
@Suppress("ComposableFunctionName")
fun ComposeScreen() = MVIComposable(
    storeViewModel<CounterContainer, _, _, _> { parametersOf("I am a parameter") }
) { state: CounterState -> // this -> ConsumerScope

    val context = LocalContext.current // we can't use composable functions in consume()
    val scaffoldState = rememberScaffoldState()

    consume { action ->
        // This block is run in a new coroutine each time we consume a new action and the lifecycle is RESUMED.
        // You can run suspending code here safely
        // consume() block will only be called when a new action is emitted (independent of recompositions)
        when (action) {
            is ShowLambdaMessage -> scaffoldState.snackbar(context.getString(R.string.lambda_message))
            is ShowErrorMessage -> scaffoldState.snackbar(context.getString(R.string.error_message))
        }
    }

    Scaffold(Modifier.fillMaxSize(), scaffoldState = scaffoldState) {
        ComposeScreenContent(state = state, modifier = Modifier.padding(it))
    }
}

@Composable
private fun Scope.ComposeScreenContent(
    state: CounterState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is DisplayingCounter -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(id = R.string.timer_template, state.timer),
                    modifier = Modifier.clickable { intent(ClickedCounter) }
                )
                Text(
                    text = stringResource(id = R.string.counter_template, state.counter),
                )

                Text(text = state.param)

                Button(onClick = { intent(ClickedCounter) }) {
                    Text(text = stringResource(id = R.string.counter_button_label))
                }
                Button(onClick = { intent(ClickedUndo) }) {
                    Text(text = stringResource(id = R.string.counter_undo_label))
                }
            }
            is CounterState.Loading -> CircularProgressIndicator()
            is CounterState.Error -> Text(state.e.message.toString())
        }
    }
}

private class PreviewProvider : StateProvider<CounterState>(
    DisplayingCounter(1, 2, "param"),
    CounterState.Loading,
)

@Composable
@Preview(name = "ComposeScreen", showSystemUi = true, showBackground = true, backgroundColor = 0xFFFFFFFF)
private fun ComposeScreenPreview(
    @PreviewParameter(PreviewProvider::class) state: CounterState,
) = MVISampleTheme {
    // Use this helper function to preview functions that use ConsumerScope
    EmptyScope {
        ComposeScreenContent(state)
    }
}
