package pro.respawn.flowmvi.sample.compose

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
import androidx.compose.runtime.getValue
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
import pro.respawn.flowmvi.android.compose.preview.StateProvider
import pro.respawn.flowmvi.android.compose.dsl.subscribe
import pro.respawn.flowmvi.android.compose.preview.EmptyReceiver
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.sample.CounterAction.GoBack
import pro.respawn.flowmvi.sample.CounterAction.ShowErrorMessage
import pro.respawn.flowmvi.sample.CounterAction.ShowLambdaMessage
import pro.respawn.flowmvi.sample.CounterIntent
import pro.respawn.flowmvi.sample.CounterIntent.ClickedBack
import pro.respawn.flowmvi.sample.CounterIntent.ClickedCounter
import pro.respawn.flowmvi.sample.CounterState
import pro.respawn.flowmvi.sample.CounterState.DisplayingCounter
import pro.respawn.flowmvi.sample.CounterState.Error
import pro.respawn.flowmvi.sample.CounterState.Loading
import pro.respawn.flowmvi.sample.R
import pro.respawn.flowmvi.sample.compose.theme.MVISampleTheme
import pro.respawn.flowmvi.sample.di.storeViewModel

context(CoroutineScope)
fun ScaffoldState.snackbar(text: String) = launch { snackbarHostState.showSnackbar(text) }

@Composable
@Suppress("ComposableFunctionName")
fun ComposeScreen(onBack: () -> Unit) {
    val store = storeViewModel<CounterContainer, _, _, _> { parametersOf("I am a parameter") }
    val context = LocalContext.current // we can't use composable functions in consume()
    val scaffoldState = rememberScaffoldState()

    val state by store.subscribe { action ->
        // This block is run in the scope of the subscription each time we consume a new action and the lifecycle is RESUMED.
        // You can run suspending code here but that will block all other actions' retrieval. Use launch { } to not block.
        // consume() block will only be called when a new action is emitted (independent of recompositions)
        when (action) {
            is ShowLambdaMessage -> scaffoldState.snackbar(context.getString(R.string.lambda_message))
            is ShowErrorMessage -> scaffoldState.snackbar(context.getString(R.string.error_message))
            is GoBack -> onBack()
        }
    }

    Scaffold(Modifier.fillMaxSize(), scaffoldState = scaffoldState) {
        store.ComposeScreenContent(state = state, modifier = Modifier.padding(it))
    }
}

@Composable
private fun IntentReceiver<CounterIntent>.ComposeScreenContent(
    state: CounterState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is Loading -> CircularProgressIndicator()
            is Error -> Text(state.e.message.toString())
            is DisplayingCounter -> Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(id = R.string.timer_template, state.timer),
                )
                Text(
                    text = stringResource(id = R.string.counter_template, state.counter),
                )
                Button(onClick = { intent(ClickedCounter) }) {
                    Text(text = stringResource(id = R.string.counter_button_label))
                }
                Button(onClick = { intent(ClickedBack) }) {
                    Text(text = stringResource(id = R.string.counter_back_label))
                }
            }
        }
    }
}

private class PreviewProvider : StateProvider<CounterState>(
    DisplayingCounter(1, 2, "param"),
    Loading,
)

@Composable
@Preview(name = "ComposeScreen", showSystemUi = true, showBackground = true, backgroundColor = 0xFFFFFFFF)
private fun ComposeScreenPreview(
    @PreviewParameter(PreviewProvider::class) state: CounterState,
) = MVISampleTheme {
    // Use this helper function to preview functions that use ConsumerScope
    EmptyReceiver {
        ComposeScreenContent(state)
    }
}
