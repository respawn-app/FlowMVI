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
import pro.respawn.flowmvi.android.compose.MVIComposable
import pro.respawn.flowmvi.android.compose.StateProvider
import pro.respawn.flowmvi.android.compose.consume
import pro.respawn.flowmvi.sample.R
import pro.respawn.flowmvi.sample.provider.CounterAction
import pro.respawn.flowmvi.sample.provider.CounterAction.ShowSnackbar
import pro.respawn.flowmvi.sample.provider.CounterIntent
import pro.respawn.flowmvi.sample.provider.CounterIntent.ClickedCounter
import pro.respawn.flowmvi.sample.provider.CounterState
import pro.respawn.flowmvi.sample.provider.CounterState.DisplayingCounter
import pro.respawn.flowmvi.sample.provider.CounterViewModel
import pro.respawn.flowmvi.sample.ui.theme.MVISampleTheme

private typealias Scope = ConsumerScope<CounterIntent, CounterAction>

@Composable
@Suppress("ComposableFunctionName")
fun ComposeScreen() = MVIComposable(getViewModel<CounterViewModel>()) { state -> // this -> ConsumerScope

    val context = LocalContext.current // we can't use composable functions in consume()
    val scaffoldState = rememberScaffoldState()

    consume { action ->
        // This block is run in a new coroutine each time we consume a new actions.
        // You can run suspending (but not blocking) code here safely
        // consume() block will only be called when a new action is emitted (independent of recompositions)
        when (action) {
            is ShowSnackbar -> launch {
                scaffoldState.snackbarHostState.showSnackbar(context.getString(action.res))
            }
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
                    // send() is available in ConsumerScope
                    modifier = Modifier.clickable { send(ClickedCounter) }
                )
                Text(
                    text = stringResource(id = R.string.counter_template, state.counter),
                )

                Button(onClick = { send(ClickedCounter) }) {
                    Text(text = stringResource(id = R.string.counter_button_label))
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
