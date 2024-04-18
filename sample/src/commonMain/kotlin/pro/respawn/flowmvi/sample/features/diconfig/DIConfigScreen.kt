package pro.respawn.flowmvi.sample.features.diconfig

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.snipme.highlights.model.PhraseLocation
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.sample.Res
import pro.respawn.flowmvi.sample.arch.di.container
import pro.respawn.flowmvi.sample.di_feature_title
import pro.respawn.flowmvi.sample.navigation.util.Navigator
import pro.respawn.flowmvi.sample.navigation.util.backNavigator
import pro.respawn.flowmvi.sample.ui.widgets.CodeText
import pro.respawn.flowmvi.sample.ui.widgets.RScaffold
import pro.respawn.flowmvi.sample.util.adaptiveWidth

private const val Description = """
FlowMVI lets you to inject and reuse store configurations.
You can inject any plugins into your store, including saved state plugin!
This allows you to provide not only test doubles for your stores, but also different configurations 
based on release/debug builds and environments.

The sample app repository sets up an injectable configuration that automatically enables remote debugging
on debug builds only and abstracts away from all the state saving logic.

It also provides a configuration for unit-testing stores.
"""

//language=kotlin
private const val Code = """
internal class DiConfigContainer(
    configuration: StoreConfiguration,
) : Container<PersistedCounterState, Nothing, Nothing> {

    override val store = store(PersistedCounterState()) {

        configure(
            configuration = configuration,
            name = "DiConfigStore",
            serializer = PersistedCounterState.serializer()
        )

        // calling configure() is equivalent to:
        
        name = "DiConfigStore"
        debuggable = BuildFlags.debuggable
        actionShareBehavior = ActionShareBehavior.Distribute()
        onOverflow = SUSPEND
        parallelIntents = true
        saveStatePlugin(
            saver = <injected saver>,
            name = "{name}SavedStatePlugin",
            context = Dispatchers.IO,
        )
        if (debuggable) {
            enableLogging()
            remoteDebugger()
        }
    }
}
"""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiConfigScreen(
    navigator: Navigator,
) = with(container<DiConfigContainer, _, _, _>()) {
    val state by subscribe()
    RScaffold(
        onBack = navigator.backNavigator,
        title = stringResource(Res.string.di_feature_title),
    ) {
        DiConfigScreenContent(state)
    }
}

@Composable
private fun DiConfigScreenContent(state: PersistedCounterState) = Column(
    modifier = Modifier
        .fillMaxHeight()
        .adaptiveWidth()
        .padding(horizontal = 12.dp)
        .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Top,
) {
    Text(Description.trimIndent())
    Spacer(Modifier.height(12.dp))
    Text("Persisted counter state: ${state.counter}")
    Spacer(Modifier.height(12.dp))
    CodeText(Code, PhraseLocation(start = 198, end = 357))
}
