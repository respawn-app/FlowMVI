package pro.respawn.flowmvi.savedstate.plugins

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.api.UnrecoverableException
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.savedstate.api.Saver
import kotlin.coroutines.CoroutineContext

@PublishedApi
internal inline fun <reified T> DefaultName(): String = "${T::class.simpleName}"

@PublishedApi
internal val PluginNameSuffix: String = "SaveStatePlugin"

@PublishedApi
internal suspend fun <S> Saver<S>.saveCatching(state: S?): Unit = try {
    save(state)
} catch (expected: Exception) {
    recover(expected)
    Unit
}

@PublishedApi
internal suspend fun <S> Saver<S>.restoreCatching(): S? = try {
    restore()
} catch (expected: Exception) {
    recover(expected)
}

@FlowMVIDSL
public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> saveStatePlugin(
    saver: Saver<S>,
    context: CoroutineContext,
    name: String = "${DefaultName<S>()}$PluginNameSuffix",
    saveOnChange: Boolean = false,
    resetOnException: Boolean = true,
): StorePlugin<S, I, A> = plugin {
    this.name = name
    onStart {
        withContext(this + context) {
            updateState { saver.restoreCatching() ?: this }
        }
    }
    if (saveOnChange) onState { _, new ->
        launch(context) { saver.saveCatching(new) }
        new
    } else onUnsubscribe {
        launch(context) { withState { saver.saveCatching(this) } }
    }
    onException {
        if (it is UnrecoverableException || resetOnException) withContext(this + context) {
            saver.saveCatching(null)
        }
        it
    }
}

@FlowMVIDSL
public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.saveState(
    saver: Saver<S>,
    context: CoroutineContext,
    name: String = "${DefaultName<S>()}$PluginNameSuffix",
    saveOnChange: Boolean = false,
    resetOnException: Boolean = true,
): Unit = install(saveStatePlugin(saver, context, name, saveOnChange, resetOnException))
