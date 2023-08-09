@file:Suppress("MemberVisibilityCanBePrivate", "NOTHING_TO_INLINE")

package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.store.StoreConfiguration
import pro.respawn.flowmvi.store.StoreImpl

public typealias BuildStore<S, I, A> = StoreBuilder<S, I, A>.() -> Unit

/**
 * A builder DSL for creating a [Store].
 * Cannot be instantiated outside of [store] functions.
 * After building, the [StoreConfiguration] is created and used in the [Store].
 * This configuration must **not** be changed in any way after the store is created through circumvention measures.
 * @param initial initial state the store will have.
 */
@FlowMVIDSL
public class StoreBuilder<S : MVIState, I : MVIIntent, A : MVIAction> @PublishedApi internal constructor(
    public val initial: S,
) {

    private var plugins: MutableSet<StorePlugin<S, I, A>> = mutableSetOf()

    /**
     * Settings this to true enables additional store validations and debug logging.
     */
    @FlowMVIDSL
    public var debuggable: Boolean = false

    /**
     * Set the future name of the store.
     * See [Store.name] for more info.
     *
     * null by default
     */
    @FlowMVIDSL
    public var name: String? = null

    /**
     * Declare that intents must be processed in parallel.
     * All guarantees on the order of [MVIIntent]s will be lost.
     * Intents may still be dropped according to [onOverflow].
     * Intents are not **obtained** in parallel, just processed.
     *
     * false by default.
     */
    @FlowMVIDSL
    public var parallelIntents: Boolean = false

    /**
     * Provide the [ActionShareBehavior] for the store.
     * For stores where actions are of type [Nothing] this must be set to [ActionShareBehavior.Disabled].
     * Will be set automatically when using the two-argument store builder.
     *
     * [ActionShareBehavior.Distribute] by default.
     */
    @FlowMVIDSL
    public var actionShareBehavior: ActionShareBehavior = ActionShareBehavior.Distribute()

    /**
     * Designate behavior for when [pro.respawn.flowmvi.api.IntentReceiver]'s [MVIIntent] pool overflows.
     *
     * [BufferOverflow.DROP_OLDEST] by default
     */
    @FlowMVIDSL
    public var onOverflow: BufferOverflow = BufferOverflow.DROP_OLDEST

    /**
     * Designate the maximum capacity of [MVIIntent]s waiting for processing
     * in the [pro.respawn.flowmvi.api.IntentReceiver]'s queue.
     * Intents that overflow this capacity will be processed according to [onOverflow].
     * This should be either a positive value, or one of:
     *  * [Channel.UNLIMITED]
     *  * [Channel.CONFLATED]
     *  * [Channel.RENDEZVOUS]
     *  * [Channel.BUFFERED]
     *
     *  [Channel.UNLIMITED] by default.
     */
    @FlowMVIDSL
    public var intentCapacity: Int = Channel.UNLIMITED

    /**
     * Install an existing [StorePlugin]. See the other overload to build the plugin on the fly.
     * This installs a prebuilt plugin.
     * Plugins will **preserve** the order of installation and will proceed according to this order.
     * See [StorePlugin] for comprehensive information on the behavior of plugins.
     * Installation of the same plugin multiple times is **not allowed**.
     * See [StorePlugin.name] for more info and solutions.
     */
    @FlowMVIDSL
    public fun install(plugin: StorePlugin<S, I, A>): Unit = require(plugins.add(plugin)) {
        duplicatePluginMessage(plugin.toString())
    }

    /**
     * Install all [plugins].
     * Please see documentation for the other overload for more details.
     * @see install
     */
    @FlowMVIDSL
    public inline fun install(vararg plugins: StorePlugin<S, I, A>): Unit = install(plugins.asIterable())

    /**
     * Install all [plugins].
     * Please see documentation for the other overload for more details.
     * @see install
     */
    @FlowMVIDSL
    public inline fun install(plugins: Iterable<StorePlugin<S, I, A>>): Unit = plugins.forEach { install(it) }

    /**
     * Create and install a new [StorePlugin].
     * Please see documentation for the other overload for more details.
     * @see install
     */
    @FlowMVIDSL
    public inline fun install(
        block: StorePluginBuilder<S, I, A>.() -> Unit
    ): Unit = install(plugin(block))

    @PublishedApi
    @FlowMVIDSL
    internal fun build(): Store<S, I, A> = StoreConfiguration(
        initial = initial,
        name = name,
        parallelIntents = parallelIntents,
        actionShareBehavior = actionShareBehavior,
        intentCapacity = intentCapacity,
        onOverflow = onOverflow,
        debuggable = debuggable,
        plugins = plugins,
    ).let(::StoreImpl)
}

private fun duplicatePluginMessage(name: String) = """
    You have attempted to install plugin $name which was already installed.
    Plugins can be repeatable if they have different names or are different instances of the target class.
    You either have installed the same plugin instance twice or have installed two plugins with the same name.
    To fix, please either create a new plugin instance for each installation (when not using names) 
    or override the plugin name to be unique among all plugins for this store.
    Consult the StorePlugin docs to learn more.
""".trimIndent()
