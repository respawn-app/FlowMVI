package pro.respawn.flowmvi.sample.features.undoredo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.sample.Res
import pro.respawn.flowmvi.sample.arch.di.container
import pro.respawn.flowmvi.sample.features.undoredo.UndoRedoIntent.ChangedInput
import pro.respawn.flowmvi.sample.features.undoredo.UndoRedoIntent.ClickedRedo
import pro.respawn.flowmvi.sample.features.undoredo.UndoRedoIntent.ClickedUndo
import pro.respawn.flowmvi.sample.navigation.util.Navigator
import pro.respawn.flowmvi.sample.navigation.util.backNavigator
import pro.respawn.flowmvi.sample.ui.widgets.CodeText
import pro.respawn.flowmvi.sample.ui.widgets.RIcon
import pro.respawn.flowmvi.sample.ui.widgets.RScaffold
import pro.respawn.flowmvi.sample.ui.widgets.RTextInput
import pro.respawn.flowmvi.sample.undoredo_feature_title
import pro.respawn.flowmvi.sample.util.adaptiveWidth

private const val Description = """
FlowMVI provides undo/redo functionality out of the box, installed as a simple plugin.
The plugin handles the queue, max undos, errors and store lifecycle for you.
Type something into the box below, then try undoing / redoing your actions.
"""

//language=kotlin
private const val Code = """
internal class UndoRedoContainer : Container<UndoRedoState, UndoRedoIntent, UndoRedoAction> {

    private val lastInput = MutableStateFlow("")

    override val store = store(UndoRedoState(lastInput.value.input())) {

        val undoRedo = undoRedo(MaxHistorySize)

        whileSubscribed { 
            coroutineScope {
                lastInput.map { old ->
                    delay(DebounceDuration)
                    withState {
                        val new = input
                        if (new.value == old) return@withState
                        undoRedo(
                            doImmediately = false,
                            redo = { useState { copy(input = new) } },
                            undo = { useState { copy(input = old.input()) } },
                        )
                    }
                }.launchIn(this)

                undoRedo.queue.onEach { (i, canUndo, canRedo) ->
                    updateState {
                        copy(index = i, canUndo = canUndo, canRedo = canRedo)
                    }
                }.launchIn(this)
            }
        }

        reduce { intent ->
            when (intent) {
                is ClickedRedo -> undoRedo.redo()
                is ClickedUndo -> undoRedo.undo()
                is ChangedInput -> useState {
                    lastInput.value = intent.value
                    copy(input = input(intent.value))
                }
            }
        }
    } 
}
"""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UndoRedoScreen(
    navigator: Navigator,
) = with(container<UndoRedoContainer, _, _, _>()) {
    val state by subscribe()

    RScaffold(
        title = stringResource(Res.string.undoredo_feature_title),
        onBack = navigator.backNavigator,
    ) {
        UndoRedoScreenContent(state)
    }
}

@Composable
private fun IntentReceiver<UndoRedoIntent>.UndoRedoScreenContent(
    state: UndoRedoState,
) = Column(
    modifier = Modifier.fillMaxHeight()
        .adaptiveWidth()
        .padding(horizontal = 12.dp)
        .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
) {
    Text(Description.trimIndent())
    Spacer(Modifier.height(12.dp))
    Row(modifier = Modifier.widthIn(min = 400.dp), horizontalArrangement = Arrangement.End) {
        Text("Index: ${state.index}")
        Spacer(Modifier.weight(1f))
        RIcon(
            icon = Icons.AutoMirrored.Rounded.Undo,
            onClick = { intent(ClickedUndo) },
            enabled = state.canUndo,
        )
        RIcon(
            icon = Icons.AutoMirrored.Rounded.Redo,
            onClick = { intent(ClickedRedo) },
            enabled = state.canRedo,
        )
    }
    RTextInput(
        input = state.input,
        onTextChange = { intent(ChangedInput(it)) },
        modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp),
        label = "Type and undo",
    )
    Spacer(Modifier.height(12.dp))
    CodeText(Code)
}
