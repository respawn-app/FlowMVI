package pro.respawn.flowmvi.sample.features.undoredo

import androidx.compose.runtime.Immutable
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.kmmutils.inputforms.Input
import pro.respawn.kmmutils.inputforms.dsl.input
import kotlin.jvm.JvmInline

@Immutable
internal data class UndoRedoState(
    val input: Input = input(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val index: Int = -1,
) : MVIState

@Immutable
internal sealed interface UndoRedoIntent : MVIIntent {

    @JvmInline
    value class ChangedInput(val value: String) : UndoRedoIntent

    data object ClickedUndo : UndoRedoIntent
    data object ClickedRedo : UndoRedoIntent
}
