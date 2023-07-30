@file:Suppress("MemberVisibilityCanBePrivate")

package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import pro.respawn.flowmvi.MutableStore
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.plugins.CompositePlugin
import pro.respawn.flowmvi.store.StoreImpl
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName

/**
 * A holder for initial values the store starts with.
 * @param state initial state of the store.
 * @see StoreBuilder.initial
 * @see [Store.initial]
 */
@JvmInline
public value class Init<S : MVIState> internal constructor(public val state: S)

public typealias BuildStore<S, I, A> = StoreBuilder<S, I, A>.() -> Init<S>

/**
 * A builder DSL for creating a [Store].
 * Cannot be instantiated outside of [store] functions.
 * After building, the [StoreConfiguration] is created and used in the [Store].
 * This configuration must **not** be changed in any way after the store is created through circumvention measures.
 */
@FlowMVIDSL
public class StoreBuilder<S : MVIState, I : MVIIntent, A : MVIAction> @PublishedApi internal constructor() {

    private var plugins: MutableSet<StorePlugin<S, I, A>> = mutableSetOf()

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
     * Designate behavior for when [pro.respawn.flowmvi.api.IntentReceiver]'s [MVIIntent] pool overflows
     * [BufferOverflow.DROP_OLDEST] by default
     */
    @FlowMVIDSL
    public var onOverflow: BufferOverflow = BufferOverflow.DROP_OLDEST

    /**
     * Designate the maximum capacity of [MVIIntent]s waiting for processing
     * in the [pro.respawn.flowmvi.api.IntentReceiver]'s queue.
     * Intents that overflow this capacity will be processed according to [onOverflow].
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
    public fun install(plugin: StorePlugin<S, I, A>) {
        require(plugins.add(plugin)) { duplicatePluginMessage(plugin.toString()) }
    }

    /**
     * Install a new [StorePlugin]. See the other overload to build the plugin on the fly.
     * This installs a prebuilt plugin.
     * Plugins will **preserve** the order of installation and will proceed according to this order.
     * See [StorePlugin] for comprehensive information on the behavior of plugins.
     */
    @FlowMVIDSL
    public fun install(
        block: StorePluginBuilder<S, I, A>.() -> Unit
    ): Unit = install(storePlugin(block))

    /**
     * Create and return an [Init] holder with initial values for the store.
     * This must be **the last statement** in this store builder's block.
     */
    @FlowMVIDSL
    public fun initial(state: S): Init<S> = Init(state)

    @PublishedApi
    @FlowMVIDSL
    internal fun build(init: Init<S>): MutableStore<S, I, A> = StoreConfiguration(
        name = name,
        initial = init.state,
        parallelIntents = parallelIntents,
        actionShareBehavior = actionShareBehavior,
        intentCapacity = intentCapacity,
        onOverflow = onOverflow,
        plugin = CompositePlugin(plugins = plugins.toSet()),
    ).let(::StoreImpl)
}

/**
 * Build a new [Store] using [StoreBuilder].
 * The store is not launched and created eagerly, with all its plugins.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> store(
    @BuilderInference crossinline configure: BuildStore<S, I, A>,
): Store<S, I, A> = StoreBuilder<S, I, A>().run {
    build(configure())
}

/**
 * * Build a new [Store] using [StoreBuilder] but disallow using [MVIAction]s.
 * The store is **not** launched and created eagerly, with all its plugins.
 *
 * If your code doesn't compile, you are looking for another overload with three type parameters, i.e:
 * `store<_, _, _>()`
 */
@FlowMVIDSL
@JvmName("noActionStore")
public inline fun <S : MVIState, I : MVIIntent> store(
    @BuilderInference crossinline configure: BuildStore<S, I, Nothing>,
): Store<S, I, Nothing> = StoreBuilder<S, I, Nothing>().run {
    val init = configure()
    actionShareBehavior = ActionShareBehavior.Disabled
    build(init)
}

/**
 * Build a new [Store] using [StoreBuilder].
 * The store is **not** launched and created lazily, with all its plugins.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyStore(
    @BuilderInference crossinline configure: BuildStore<S, I, A>,
): Lazy<Store<S, I, A>> = lazy { store(configure) }

/**
 * Build a new [Store] using [StoreBuilder].
 * The store is built **lazily** and launched on **first access** (i.e. after bulding).
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyStore(
    scope: CoroutineScope,
    @BuilderInference crossinline configure: BuildStore<S, I, A>,
): Lazy<Store<S, I, A>> = lazy { store(configure).apply { start(scope) } }

private fun duplicatePluginMessage(name: String) = """
    You have attempted to install plugin $name which was already installed.
    Plugins can be repeatable if they have different names or are different instances of the target class.
    You either have installed the same plugin instance twice or have installed two plugins with the same name.
    To fix, please either create a new plugin instance for each installation (when not using names) 
    or override the plugin name to be unique among all plugins for this store.
    Consult the StorePlugin docs to learn more
""".trimIndent()
