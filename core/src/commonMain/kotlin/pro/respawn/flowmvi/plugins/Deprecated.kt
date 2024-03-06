@file:Suppress("DEPRECATION")

package pro.respawn.flowmvi.plugins

import kotlinx.coroutines.coroutineScope
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.subscribe
import pro.respawn.flowmvi.logging.ConsoleStoreLogger
import pro.respawn.flowmvi.logging.PlatformStoreLogger
import pro.respawn.flowmvi.logging.StoreLogLevel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.dsl.plugin
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A logging plugin that prints logs to the console using [println]. Tag is not used except for naming the plugin.
 * @see loggingPlugin
 */
@FlowMVIDSL
@Deprecated(
    "Just use logging plugin with ConsoleStoreLogger from now on",
    ReplaceWith("loggingPlugin(ConsoleStoreLogger, tag, name, level)")
)
public fun <S : MVIState, I : MVIIntent, A : MVIAction> consoleLoggingPlugin(
    tag: String? = null,
    name: String = "${tag.orEmpty()}Logging",
    level: StoreLogLevel? = null,
): StorePlugin<S, I, A> = loggingPlugin(ConsoleStoreLogger, tag, name, level)

/**
 * Creates a [loggingPlugin] that is suitable for each targeted platform.
 * This plugin will log to:
 * * Logcat on Android,
 * * NSLog on apple targets,
 * * console.log on JS,
 * * stdout on mingw/native
 * * System.out on JVM.
 */
@Deprecated(
    "Just use logging plugin and a platform store logger from now on",
    ReplaceWith("loggingPlugin()")
)
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> platformLoggingPlugin(
    tag: String? = null,
    name: String = "${tag.orEmpty()}Logging",
    level: StoreLogLevel? = null
): StorePlugin<S, I, A> = loggingPlugin(PlatformStoreLogger, tag, name, level)

/**
 * A base class for creating custom [StorePlugin]s.
 *
 * It is preferred to use composition instead of inheriting this class.
 * Prefer [pro.respawn.flowmvi.dsl.plugin] builder function instead of extending this class.
 * For an example, see how a [jobManagerPlugin] ([JobManager]) is implemented.
 *
 * @see [StorePlugin]
 * @see [pro.respawn.flowmvi.dsl.plugin]
 */
@Deprecated(
    """
Plugin builders provide sufficient functionality to use them instead of this class.
Extending this class limits your API and leaks lifecycle methods of a plugin to external code.   
This class will become internal in future releases of the library.
"""
)
public abstract class AbstractStorePlugin<S : MVIState, I : MVIIntent, A : MVIAction>(
    final override val name: String? = null,
) : StorePlugin<S, I, A> {

    final override fun toString(): String = "StorePlugin \"${name ?: super.toString()}\""
    final override fun hashCode(): Int = name?.hashCode() ?: super.hashCode()
    final override fun equals(other: Any?): Boolean = when {
        other !is StorePlugin<*, *, *> -> false
        other.name == null && name == null -> this === other
        else -> name == other.name
    }
}

/**
 * An overload of the [parentStorePlugin] that also consumes its [MVIAction]s.
 * Please see the other overload for more documentation.
 *
 * @see parentStorePlugin
 * @see parentStore
 */
@Suppress("Indentation") // conflicts with IDE formatting
@FlowMVIDSL
public inline fun <
    S : MVIState,
    I : MVIIntent,
    A : MVIAction,
    S2 : MVIState,
    I2 : MVIIntent,
    A2 : MVIAction
    > parentStorePlugin(
    parent: Store<S2, I2, A2>,
    name: String? = parent.name?.let { "ParentStorePlugin\$$it" },
    minExternalSubscriptions: Int = 1,
    @BuilderInference crossinline consume: suspend PipelineContext<S, I, A>.(action: A2) -> Unit,
    @BuilderInference crossinline render: suspend PipelineContext<S, I, A>.(state: S2) -> Unit,
): StorePlugin<S, I, A> = whileSubscribedPlugin(name = name, minSubscriptions = minExternalSubscriptions) {
    // do not use pipeline context to cancel subscription properly, suspend instead
    coroutineScope {
        subscribe(parent, { consume(it) }, { render(it) }).join()
    }
}

/**
 * Creates and installs a new plugin that will subscribe to the [parent] store at the same time the current store is
 * subscribed to and when [minExternalSubscriptions] are reached.
 *
 * This plugin will subscribe to the parent store and [render] its states while there are [minExternalSubscriptions] of
 * the current store present.
 * When the subscribers leave as in [whileSubscribedPlugin], this store will also unsubscribe from the parent store.
 *
 * Essentially, this store will be a subscriber of another store while this store is also subscribed to externally.
 * For the behavior where the store will always be subscribed, please subscribe in the [initPlugin].
 *
 * This function will not consume [MVIAction]s of the parent store. For that, please see the other overload of this
 * function.
 *
 * The name of this plugin will be derived from the parent store's name, if present, otherwise `null`.
 *
 * @see parentStorePlugin
 * @see parentStore
 */
@Suppress("Indentation") // conflicts with IDE formatting
@FlowMVIDSL
@Deprecated(
    """
    Parent store plugin introduces unnecessary complexity to the behavior and has little flexibility.
    Subscribe to the store in some other plugin instead, such as whileSubscribedPlugin using a suspending function collect()
    """,
    ReplaceWith(
        "whileSubscribedPlugin { parent.collect { } }",
        "pro.respawn.flowmvi.plugins.whileSubscribedPlugin",
        "pro.respawn.flowmvi.dsl.collect"
    )
)
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction, S2 : MVIState, I2 : MVIIntent> parentStorePlugin(
    parent: Store<S2, I2, *>,
    name: String? = parent.name?.let { "ParentStorePlugin\$$it" },
    minExternalSubscriptions: Int = 1,
    @BuilderInference crossinline render: suspend PipelineContext<S, I, A>.(state: S2) -> Unit,
): StorePlugin<S, I, A> = whileSubscribedPlugin(name = name, minSubscriptions = minExternalSubscriptions) {
    coroutineScope {
        subscribe(parent, render = { render(it) }).join()
    }
}

/**
 * Install a new [parentStorePlugin]. This overload **does not** collect the parent store's actions.
 * @see parentStorePlugin
 */
@Deprecated(
    """
    Parent store plugin introduces unnecessary complexity to the behavior and has little flexibility.
    Subscribe to the store in some other plugin instead, such as whileSubscribedPlugin using a suspending function collect()
    """,
    ReplaceWith(
        "whileSubscribed { parent.collect { } }",
        "pro.respawn.flowmvi.plugins.whileSubscribed",
        "pro.respawn.flowmvi.dsl.collect"
    )
)
@Suppress("Indentation") // conflicts with IDE formatting
@FlowMVIDSL
public inline fun <
    S : MVIState,
    I : MVIIntent,
    A : MVIAction,
    S2 : MVIState,
    I2 : MVIIntent,
    > StoreBuilder<S, I, A>.parentStore(
    parent: Store<S2, I2, *>,
    name: String? = parent.name?.let { "ParentStorePlugin\$$it" },
    minExternalSubscriptions: Int = 1,
    @BuilderInference crossinline render: suspend PipelineContext<S, I, A>.(state: S2) -> Unit,
): Unit = install(parentStorePlugin(parent, name, minExternalSubscriptions, render = render))

/**
 * Install a new [parentStorePlugin].
 * @see parentStorePlugin
 */
@Deprecated(
    """
    Parent store plugin introduces unnecessary complexity to the behavior and has little flexibility.
    Subscribe to the store in some other plugin instead, such as whileSubscribedPlugin using a suspending function collect()
    """,
    ReplaceWith(
        "whileSubscribed { parent.collect { } }",
        "pro.respawn.flowmvi.plugins.whileSubscribed",
        "pro.respawn.flowmvi.dsl.collect"
    )
)
@Suppress("Indentation") // conflicts with IDE formatting
@FlowMVIDSL
public inline fun <
    S : MVIState,
    I : MVIIntent,
    A : MVIAction,
    S2 : MVIState,
    I2 : MVIIntent,
    A2 : MVIAction,
    > StoreBuilder<S, I, A>.parentStore(
    parent: Store<S2, I2, A2>,
    name: String? = parent.name?.let { "ParentStorePlugin\$$it" },
    minExternalSubscriptions: Int = 1,
    @BuilderInference crossinline consume: suspend PipelineContext<S, I, A>.(action: A2) -> Unit,
    @BuilderInference crossinline render: suspend PipelineContext<S, I, A>.(state: S2) -> Unit,
): Unit = install(parentStorePlugin(parent, name, minExternalSubscriptions, consume = consume, render = render))

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
@Suppress("DEPRECATION")
@Deprecated("If you want to save state, use the new `savedstate` module dependency")
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.saveState(
    name: String = DefaultSavedStatePluginName,
    context: CoroutineContext = EmptyCoroutineContext,
    @BuilderInference crossinline get: suspend S.() -> S?,
    @BuilderInference crossinline set: suspend S.() -> Unit,
): Unit = install(savedStatePlugin(name, context, get, set))
