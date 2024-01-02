package pro.respawn.flowmvi.plugins

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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
@Deprecated("If you want to save state, use the new `savedstate` module dependency")
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> savedStatePlugin(
    name: String = DefaultSavedStatePluginName,
    context: CoroutineContext = EmptyCoroutineContext,
    @BuilderInference crossinline get: suspend S.() -> S?,
    @BuilderInference crossinline set: suspend (S) -> Unit,
): StorePlugin<S, I, A> = plugin {
    this.name = name
    onState { _, new ->
        launch(context) { set(new) }
        new
    }
    onStart {
        withContext(context) {
            updateState {
                get() ?: this
            }
        }
    }
}

/**
 * Creates and installs a new [savedStatePlugin].
 */
@FlowMVIDSL
@Deprecated("If you want to save state, use the new `savedstate` module dependency")
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.saveState(
    name: String = DefaultSavedStatePluginName,
    context: CoroutineContext = EmptyCoroutineContext,
    @BuilderInference crossinline get: suspend S.() -> S?,
    @BuilderInference crossinline set: suspend S.() -> Unit,
): Unit = install(savedStatePlugin(name, context, get, set))
