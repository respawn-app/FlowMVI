package pro.respawn.flowmvi.plugins

import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin

/**
 * Default name for the SavedStatePlugin
 */
public const val DefaultSavedStatePluginName: String = "SavedState"

/**
 * A plugin that restores the [pro.respawn.flowmvi.api.StateProvider.state] using [get] in [StorePlugin.onStart]
 * and saves using [set] asynchronously in [StorePlugin.onState].
 * There are platform overloads for this function.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> savedStatePlugin(
    name: String = DefaultSavedStatePluginName,
    crossinline get: S.() -> S?,
    crossinline set: (S) -> Unit,
): StorePlugin<S, I, A> = plugin {
    this.name = name
    onState { _, new ->
        launch { set(new) }
        new
    }
    onStart {
        updateState {
            get() ?: this
        }
    }
}

/**
 * Creates and installs a new [savedStatePlugin].
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.saveState(
    name: String = DefaultSavedStatePluginName,
    crossinline get: S.() -> S?,
    crossinline set: S.() -> Unit,
): Unit = install(savedStatePlugin(name, get, set))
