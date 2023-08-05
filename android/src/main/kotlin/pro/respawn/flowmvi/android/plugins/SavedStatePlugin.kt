package pro.respawn.flowmvi.android.plugins

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.plugins.savedStatePlugin
import java.io.Serializable

/**
 * A plugin that restores the [pro.respawn.flowmvi.api.StateProvider.state] from a [handle] in [StorePlugin.onStart]
 * and saves into [handle] asynchronously in [StorePlugin.onState].
 * Your state must be [Parcelable] to use this.
 * @see savedStatePlugin
 */
@FlowMVIDSL
public fun <S, I : MVIIntent, A : MVIAction> parcelizeStatePlugin(
    key: String,
    handle: SavedStateHandle,
    name: String = "${key}SavedState",
): StorePlugin<S, I, A> where S : MVIState, S : Parcelable = savedStatePlugin(
    name = name,
    get = { handle[key] },
    set = { handle[key] = it }
)

/**
 * A plugin that restores the [pro.respawn.flowmvi.api.StateProvider.state] from a [handle] in [StorePlugin.onStart]
 * and saves into [handle] asynchronously in [StorePlugin.onState].
 * Your state must be [Serializable] to use this
 * @see savedStatePlugin
 */
@FlowMVIDSL
public fun <S, I : MVIIntent, A : MVIAction> serializeStatePlugin(
    key: String,
    handle: SavedStateHandle,
    name: String = "${key}SavedState",
): StorePlugin<S, I, A> where S : MVIState, S : Serializable = savedStatePlugin(
    name = name,
    get = { handle[key] },
    set = { handle[key] = it }
)

/**
 * Creates and installs a new [savedStatePlugin].
 * Your state must be [Parcelable] to use this.
 * @see savedStatePlugin
 */
@FlowMVIDSL
public fun <S, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.parcelizeState(
    handle: SavedStateHandle,
    key: String = "${this.name.orEmpty()}State",
    name: String = "${key}SavedState",
): Unit where S : MVIState, S : Parcelable = install(parcelizeStatePlugin(key, handle, name))

/**
 * Creates and installs a new [savedStatePlugin].
 * Your state must be [Serializable] to use this.
 * @see savedStatePlugin
 */
@FlowMVIDSL
public fun <S, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.serializeState(
    handle: SavedStateHandle,
    key: String = "${name.orEmpty()}State",
): Unit where S : MVIState, S : Serializable = install(serializeStatePlugin(key, handle))
