package pro.respawn.flowmvi.android.plugins

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.storePlugin

/**
 * A plugin that restores the [pro.respawn.flowmvi.api.StateProvider.state] from a [handle]
 * and saves into [handle] on each state change (as a background process)
 */
@FlowMVIDSL
public fun <S, I : MVIIntent, A : MVIAction> savedStatePlugin(
    key: String,
    handle: SavedStateHandle,
): StorePlugin<S, I, A> where S : MVIState, S : Parcelable = storePlugin {
    onState { _, new ->
        launch(this + Dispatchers.IO) { handle[key] = new }
        new
    }
    onStart {
        updateState {
            handle.get<S>(key) ?: this
        }
    }
}

/**
 * Creates and installs a new [savedStatePlugin].
 */
@FlowMVIDSL
public fun <S, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.saveState(
    handle: SavedStateHandle,
    key: String = "${name.orEmpty()}State",
): Unit where S : MVIState, S : Parcelable = install(
    savedStatePlugin(key, handle)
)
