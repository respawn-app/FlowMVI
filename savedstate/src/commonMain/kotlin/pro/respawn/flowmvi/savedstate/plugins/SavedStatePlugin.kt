package pro.respawn.flowmvi.savedstate.plugins

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
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
import pro.respawn.flowmvi.savedstate.api.SaveBehavior
import pro.respawn.flowmvi.savedstate.api.SaveBehavior.OnChange
import pro.respawn.flowmvi.savedstate.api.SaveBehavior.OnUnsubscribe
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
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    name: String = "${DefaultName<S>()}$PluginNameSuffix",
    resetOnException: Boolean = true,
): StorePlugin<S, I, A> = plugin {
    this.name = name
    onStart {
        withContext(this + context) {
            updateState { saver.restoreCatching() ?: this }
        }
    }
    onException {
        if (it !is UnrecoverableException && !resetOnException) return@onException it
        withContext(this + context) { saver.saveCatching(null) }
        it
    }
    val onUnsubscribe = behaviors.filterIsInstance<OnUnsubscribe>()
    if (onUnsubscribe.isNotEmpty()) onUnsubscribe { remainingSubs ->
        val shouldSave = onUnsubscribe.any { remainingSubs <= it.remainingSubscribers }
        if (!shouldSave) return@onUnsubscribe
        launch(context) { withState { saver.saveCatching(this) } }
    }
    val saveTimeout = behaviors
        .asSequence()
        .filterIsInstance<OnChange>()
        .minOfOrNull { it.timeout }
        ?: return@plugin

    var job: Job? by atomic(null)
    onState { _, new ->
        job?.cancelAndJoin()
        job = launch(context) {
            delay(saveTimeout)
            withState { saver.saveCatching(this) }
        }
        new
    }
}

@FlowMVIDSL
public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.saveState(
    saver: Saver<S>,
    context: CoroutineContext,
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    name: String = "${DefaultName<S>()}$PluginNameSuffix",
    resetOnException: Boolean = true,
): Unit = install(saveStatePlugin(saver, context, behaviors, name, resetOnException))
