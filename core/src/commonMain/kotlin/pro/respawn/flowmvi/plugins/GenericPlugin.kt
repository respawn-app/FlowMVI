package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin

/**
 * A builder of a generic [StorePlugin] that can be used with any store.
 * Due to the risk of mangling with the generic store's properties, this plugin cannot affect the store in any way.
 * The types of intents are also erased.
 */
public class GenericPluginBuilder @PublishedApi internal constructor() {

    private var intent: suspend (MVIIntent) -> Unit = {}
    private var state: suspend (old: MVIState, new: MVIState) -> Unit = { _, _ -> }
    private var action: suspend (MVIAction) -> Unit = {}
    private var exception: suspend (e: Exception) -> Unit = {}
    private var start: suspend () -> Unit = {}
    private var subscribe: (subscriptionCount: Int) -> Unit = {}
    private var unsubscribe: (subscriptionCount: Int) -> Unit = {}
    private var stop: (e: Exception?) -> Unit = {}

    /**
     * This plugin's name.
     * @see StorePlugin.name
     */
    public var name: String? = null

    /**
     * Same as [StorePlugin.onIntent], but for generic plugins
     */
    @FlowMVIDSL
    public fun onIntent(block: suspend (intent: MVIIntent) -> Unit) {
        intent = block
    }

    /**
     * Same as [StorePlugin.onState], but for generic plugins
     */
    @FlowMVIDSL
    public fun onState(block: suspend (old: MVIState, new: MVIState) -> Unit) {
        state = block
    }

    /**
     * Same as [StorePlugin.onStart], but for generic plugins
     */
    @FlowMVIDSL
    public fun onStart(block: suspend () -> Unit) {
        start = block
    }

    /**
     * Same as [StorePlugin.onStop], but for generic plugins
     */
    @FlowMVIDSL
    public fun onStop(block: (e: Exception?) -> Unit) {
        stop = block
    }

    /**
     * Same as [StorePlugin.onException], but for generic plugins
     */
    @FlowMVIDSL
    public fun onException(block: suspend (e: Exception) -> Unit) {
        exception = block
    }

    /**
     * Same as [StorePlugin.onAction], but for generic plugins
     */
    @FlowMVIDSL
    public fun onAction(block: suspend (action: MVIAction) -> Unit) {
        action = block
    }

    /**
     * Same as [StorePlugin.onSubscribe], but for generic plugins
     */
    @FlowMVIDSL
    public fun onSubscribe(block: (subscriptionCount: Int) -> Unit) {
        subscribe = block
    }

    /**
     * Same as [StorePlugin.onUnsubscribe]
     */
    @FlowMVIDSL
    public fun onUnsubscribe(block: (subscriptionCount: Int) -> Unit) {
        unsubscribe = block
    }

    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal fun <S : MVIState, I : MVIIntent, A : MVIAction> build(): StorePlugin<S, I, A> = plugin {
        name = this@GenericPluginBuilder.name
        onIntent {
            this@GenericPluginBuilder.intent(it)
            it
        }
        onAction {
            this@GenericPluginBuilder.action(it)
            it
        }
        onState { old, new ->
            state(old, new)
            new
        }
        onException {
            exception(it)
            it
        }
        onSubscribe { _, it -> subscribe(it) }
        onUnsubscribe { unsubscribe(it) }
        onStart { start() }
        onStop { stop(it) }
        // we can safely cast as this plugin can't affect the store in any way
    } as StorePlugin<S, I, A>
}

/**
 * Create a new [GenericPluginBuilder].
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> genericPlugin(
    @BuilderInference builder: GenericPluginBuilder.() -> Unit,
): StorePlugin<S, I, A> = GenericPluginBuilder().apply(builder).build()

/**
 * Create a new [genericPlugin] and install it.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.genericPlugin(
    @BuilderInference plugin: GenericPluginBuilder.() -> Unit,
): Unit = install(genericPlugin(builder = plugin))
