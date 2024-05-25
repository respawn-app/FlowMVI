package pro.respawn.flowmvi.sample.features.undoredo

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.undoRedo
import pro.respawn.flowmvi.plugins.whileSubscribed
import pro.respawn.flowmvi.sample.arch.configuration.ConfigurationFactory
import pro.respawn.flowmvi.sample.arch.configuration.configure
import pro.respawn.flowmvi.sample.features.undoredo.UndoRedoIntent.ChangedInput
import pro.respawn.flowmvi.sample.features.undoredo.UndoRedoIntent.ClickedRedo
import pro.respawn.flowmvi.sample.features.undoredo.UndoRedoIntent.ClickedUndo
import pro.respawn.kmmutils.inputforms.dsl.input

internal class UndoRedoContainer(
    configuration: ConfigurationFactory,
) : Container<UndoRedoState, UndoRedoIntent, Nothing> {

    private val lastInput = MutableStateFlow("")

    override val store = store(UndoRedoState(lastInput.value.input())) {
        configure(configuration, "UndoRedoStore")

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
                            redo = { updateStateImmediate { copy(input = new) } },
                            undo = { updateStateImmediate { copy(input = old.input()) } },
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

        recover {
            updateState { UndoRedoState() }
            null
        }

        reduce { intent ->
            when (intent) {
                is ClickedRedo -> undoRedo.redo()
                is ClickedUndo -> undoRedo.undo()
                is ChangedInput -> updateStateImmediate {
                    lastInput.value = intent.value
                    copy(input = input(intent.value))
                }
            }
        }
    }

    companion object {

        const val DebounceDuration = 1000L
        const val MaxHistorySize = 20
    }
}
