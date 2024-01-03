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
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.savedstate.api.SaveBehavior
import pro.respawn.flowmvi.savedstate.api.SaveBehavior.OnChange
import pro.respawn.flowmvi.savedstate.api.SaveBehavior.OnUnsubscribe
import pro.respawn.flowmvi.savedstate.api.Saver
import pro.respawn.flowmvi.savedstate.dsl.CompressedFileSaver
import pro.respawn.flowmvi.savedstate.dsl.DefaultFileSaver
import pro.respawn.flowmvi.savedstate.dsl.FileSaver
import pro.respawn.flowmvi.savedstate.dsl.JsonSaver
import pro.respawn.flowmvi.savedstate.dsl.MapSaver
import pro.respawn.flowmvi.savedstate.dsl.NoOpSaver
import pro.respawn.flowmvi.savedstate.dsl.TypedSaver
import kotlin.coroutines.CoroutineContext

@PublishedApi
internal inline fun <reified T> nameByType(): String? = T::class.simpleName?.removeSuffix("State")

@PublishedApi
internal val PluginNameSuffix: String = "SaveStatePlugin"

@PublishedApi
internal const val EmptyBehaviorsMessage: String = """
You wanted to save the state but have not provided any behaviors.
Please supply at least one behavior or remove the plugin as it would do nothing otherwise.
"""

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

/**
 * Creates a plugin for persisting and restoring [MVIState] of the current store.
 *
 * This function takes a [Saver] as a parameter, which it will use for determining how and where to save the state.
 * [Saver]s can be decorated and extended to implement your own logic. There are a couple of default savers:
 *  * [MapSaver] for saving partial data.
 *  * [TypedSaver] for saving a state of a particular subtype.
 *  * [JsonSaver] for saving the state as a JSON.
 *  * [FileSaver] for saving the state to a file. See [DefaultFileSaver] for custom file writing logic.
 *  * [CompressedFileSaver] for saving the state to a file and compressing it.
 *  * [NoOpSaver] for testing.
 *
 *  The plugin will determine **when** to save the state based on [behaviors].
 *  Please see [SaveBehavior] documentation for more details.
 *  this function will throw if the [behaviors] are empty.
 * ----
 *  * By default, the plugin will  use the name derived from the store's name, or the state [S] class name.
 *  * If [resetOnException] is `true`, the plugin will attempt to clear the state if an exception is thrown.
 *  * All state saving is done in a background coroutine.
 *  * The state **restoration**, however, is done **before** the store starts.
 *    This means that while the state is being restored, the store will not process intents and state changes.
 *  * The installation order of this plugin is **very** important. If other plugins, installed **after** this one,
 *  change the state in [StorePlugin.onStart], your restored state may be overwritten.
 *
 *  @see [Saver]
 */
@FlowMVIDSL
public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> saveStatePlugin(
    saver: Saver<S>,
    context: CoroutineContext,
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    name: String? = "${nameByType<S>().orEmpty()}$PluginNameSuffix",
    resetOnException: Boolean = true,
): StorePlugin<S, I, A> = plugin {
    require(behaviors.isNotEmpty()) { EmptyBehaviorsMessage }
    this.name = name
    var job: Job? by atomic(null)

    onStart {
        withContext(this + context) {
            updateState { saver.restoreCatching() ?: this }
        }
    }
    if (resetOnException) onException {
        withContext(this + context) { saver.saveCatching(null) }
        it
    }

    val maxSubscribers = behaviors
        .asSequence()
        .filterIsInstance<OnUnsubscribe>()
        .maxOfOrNull { it.remainingSubscribers }
        ?.also { require(it >= 0) { "Subscriber count must be >= 0" } }
    if (maxSubscribers != null) onUnsubscribe { remainingSubs ->
        if (remainingSubs > maxSubscribers) return@onUnsubscribe
        job?.cancelAndJoin()
        job = launch(context) { withState { saver.saveCatching(this) } }
    }

    val saveTimeout = behaviors
        .asSequence()
        .filterIsInstance<OnChange>()
        .minOfOrNull { it.delay }
        ?.also { require(!it.isNegative() && it.isFinite()) { "Delay must be >= 0" } }
    if (saveTimeout != null) onState { _, new ->
        job?.cancelAndJoin()
        job = launch(context) {
            delay(saveTimeout)
            // defer state read until delay has passed
            withState { saver.saveCatching(this) }
        }
        new
    }
}

/**
 * Creates and installs a new [saveStatePlugin]. Please see the parent overload for more info.
 *
 * @see saveStatePlugin
 */
@FlowMVIDSL
public inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.saveState(
    saver: Saver<S>,
    context: CoroutineContext,
    behaviors: Set<SaveBehavior> = SaveBehavior.Default,
    name: String? = "${this.name ?: nameByType<S>().orEmpty()}$PluginNameSuffix",
    resetOnException: Boolean = true,
): Unit = install(saveStatePlugin(saver, context, behaviors, name, resetOnException))
