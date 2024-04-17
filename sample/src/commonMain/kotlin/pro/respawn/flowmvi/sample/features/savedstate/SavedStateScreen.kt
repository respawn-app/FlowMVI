package pro.respawn.flowmvi.sample.features.savedstate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.snipme.highlights.model.PhraseLocation
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.requireLifecycle
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.sample.BuildFlags
import pro.respawn.flowmvi.sample.arch.di.container
import pro.respawn.flowmvi.sample.features.savedstate.SavedStateFeatureState.DisplayingInput
import pro.respawn.flowmvi.sample.features.savedstate.SavedStateIntent.ChangedInput
import pro.respawn.flowmvi.sample.generated.resources.Res
import pro.respawn.flowmvi.sample.generated.resources.savedstate_feature_title
import pro.respawn.flowmvi.sample.navigation.util.Navigator
import pro.respawn.flowmvi.sample.navigation.util.backNavigator
import pro.respawn.flowmvi.sample.ui.widgets.CodeText
import pro.respawn.flowmvi.sample.ui.widgets.RScaffold
import pro.respawn.flowmvi.sample.ui.widgets.RTextInput
import pro.respawn.flowmvi.sample.ui.widgets.TypeCrossfade
import pro.respawn.flowmvi.sample.util.Platform
import pro.respawn.flowmvi.sample.util.adaptiveWidth
import pro.respawn.flowmvi.sample.util.platform

private const val Description = """
    Saved state plugin allows you to persist a state of a store into a file or other place in about 5 lines of code
    
    The state is automatically compressed, written, and then restored when your store starts, all in the background
    
    You can decorate state saving/restoration logic with custom Savers and inject it transparently to the rest 
    of the code of your app
    
    Try typing something into the box below and then reopen the app - your input will be remembered!
"""

//language=kotlin
private const val Code = """
internal class SavedStateContainer(
    fileManager: FileManager,
) : Container<State, Intent, Nothing> {

    override val store = store(DisplayingInput()) {

        serializeState(
            dir = fileManager.cacheDir("state"),
            serializer = DisplayingInput.serializer(),
        )

        reduce { intent ->
            when (intent) {
                is ChangedInput -> updateState<DisplayingInput, _> {
                    copy(input = input(intent.value))
                }
            }
        }
    }
}
"""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedStateScreen(
    navigator: Navigator,
) = with(container<SavedStateContainer, _, _, _>()) {
    val state by subscribe(requireLifecycle())

    RScaffold(
        title = stringResource(Res.string.savedstate_feature_title),
        onBack = navigator.backNavigator
    ) {
        SavedStateScreenContent(state)
    }
}

@Composable
private fun IntentReceiver<SavedStateIntent>.SavedStateScreenContent(
    state: SavedStateFeatureState,
) = TypeCrossfade(state) {
    when (this) {
        is DisplayingInput -> Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).adaptiveWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(Description.trimIndent())
            Spacer(Modifier.height(24.dp))
            if (BuildFlags.platform == Platform.Web) {
                Text(
                    text = """
                        You are on web, where persisting files to file system is not supported, 
                        so the data will not be saved
                    """.trimIndent(),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            RTextInput(
                input = input,
                onTextChange = { intent(ChangedInput(it)) },
                label = "Type to save",
                modifier = Modifier.width(400.dp),
            )
            Spacer(Modifier.height(24.dp))
            @Suppress("MagicNumber")
            CodeText(Code, PhraseLocation(167, 297))
        }
    }
}
