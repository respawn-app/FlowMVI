package pro.respawn.flowmvi.sample.features.simple

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.requireLifecycle
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.sample.generated.resources.Res
import pro.respawn.flowmvi.sample.generated.resources.simple_feature_title
import pro.respawn.flowmvi.sample.navigation.util.Navigator
import pro.respawn.flowmvi.sample.navigation.util.backNavigator
import pro.respawn.flowmvi.sample.ui.widgets.CodeText
import pro.respawn.flowmvi.sample.ui.widgets.ROutlinedButton
import pro.respawn.flowmvi.sample.ui.widgets.RScaffold
import pro.respawn.flowmvi.sample.util.adaptiveWidth

//language=kotlin
private const val Code = """
data class SimpleState(val counter: Int = 0) : MVIState

sealed interface SimpleIntent : MVIIntent {

    data object ClickedButton : SimpleIntent
}

val simpleStore = store<_, _>(SimpleState()) {
    reduce { intent: SimpleIntent ->
        when (intent) {
            ClickedButton -> updateState {
                copy(counter = counter + 1)
            }
        }
    }
}
"""

private val Description = """
    Simple Feature showcases how you can start using FlowMVI with the bare minimum.
    FlowMVI allows you to create full-fledged multiplatform MVI business 
    logic components in about 10 lines of code.
    See other features for advanced examples of LCE, DI, saving state & lots of other stuff.
""".trimIndent()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleScreen(
    navigator: Navigator,
) = with(simpleStore) {
    LaunchedEffect(Unit) { start(this).join() }

    val state by subscribe(requireLifecycle())

    RScaffold(
        title = stringResource(Res.string.simple_feature_title),
        onBack = navigator.backNavigator,
    ) {
        SimpleScreenContent(state)
    }
}

@Composable
private fun IntentReceiver<SimpleIntent>.SimpleScreenContent(
    state: SimpleState,
) = Column(
    modifier = Modifier
        .fillMaxHeight()
        .adaptiveWidth()
        .padding(horizontal = 12.dp)
        .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(Description)
    Spacer(Modifier.height(12.dp))
    Text(text = "Counter = ${state.counter}")
    Spacer(Modifier.height(12.dp))
    ROutlinedButton(
        onClick = { intent(SimpleIntent.ClickedButton) },
        content = { Text("Increment counter") },
    )
    Spacer(Modifier.height(12.dp))
    CodeText(code = Code)
}
