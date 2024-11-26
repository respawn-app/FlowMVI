package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.channels.BufferOverflow
import pro.respawn.flowmvi.StoreImpl
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.LazyPlugin
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.decorator.DecoratorBuilder
import pro.respawn.flowmvi.decorator.PluginDecorator
import pro.respawn.flowmvi.decorator.decoratedWith
import pro.respawn.flowmvi.decorator.decorator
import pro.respawn.flowmvi.impl.plugin.asInstance
import pro.respawn.flowmvi.impl.plugin.compose
import pro.respawn.flowmvi.logging.NoOpStoreLogger
import pro.respawn.flowmvi.logging.PlatformStoreLogger
import pro.respawn.flowmvi.logging.StoreLogger
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmName

public typealias BuildStore<S, I, A> = StoreBuilder<S, I, A>.() -> Unit

private const val ConfigDeprecation = """
Please use a `configure { }` block instead to set up store configuration properties and place all the setup logic there.
This is needed to prevent store body internals from accessing the builder above, especially when using lazy plugins.
Accessing these properties outside of `configure` block can lead to scoping and mutability issues.
Removal cycle: 2 releases.
"""

private fun duplicatePluginMessage(type: String, name: String) {
    val title = type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    """
        You have attempted to install $type $name which was already installed.
        $title can be repeatable if they have different names or are different instances of the target class.
        You either have installed the same $type instance twice or have installed two ${type}s with the same name.
        To fix, please either create a new $type instance for each installation (when not using names) 
        or override the $type name to be unique among all ${type}s for this store.
        Consult the Store$title docs to learn more.
    """.trimIndent()
}

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

    @PublishedApi
    internal var config: StoreConfigurationBuilder = StoreConfigurationBuilder()
        private set

    private var plugins: MutableSet<LazyPlugin<S, I, A>> = mutableSetOf()
    private var decorators: MutableSet<PluginDecorator<S, I, A>> = mutableSetOf()

    /**
     * Adjust the current [StoreConfiguration] of this [Store].
     */
    @FlowMVIDSL
    public inline fun configure(block: StoreConfigurationBuilder.() -> Unit): Unit = config.run(block)

    // region Deprecated props

    @FlowMVIDSL
    @Deprecated(ConfigDeprecation)
    @Suppress("UndocumentedPublicProperty")
    public var logger: StoreLogger
        get() = config.logger ?: if (config.debuggable) PlatformStoreLogger else NoOpStoreLogger
        set(value) {
            config.logger = value
        }

    @FlowMVIDSL
    @Deprecated(ConfigDeprecation)
    @Suppress("UndocumentedPublicProperty")
    public var coroutineContext: CoroutineContext by config::coroutineContext

    @FlowMVIDSL
    @Deprecated(ConfigDeprecation)
    @Suppress("UndocumentedPublicProperty")
    public var debuggable: Boolean by config::debuggable

    @FlowMVIDSL
    @Deprecated(ConfigDeprecation)
    @Suppress("UndocumentedPublicProperty")
    public var name: String? by config::name

    @FlowMVIDSL
    @Deprecated(ConfigDeprecation)
    @Suppress("UndocumentedPublicProperty")
    public var parallelIntents: Boolean by config::parallelIntents

    @FlowMVIDSL
    @Deprecated(ConfigDeprecation)
    @Suppress("UndocumentedPublicProperty")
    public var actionShareBehavior: ActionShareBehavior by config::actionShareBehavior

    @FlowMVIDSL
    @Deprecated(ConfigDeprecation)
    @Suppress("UndocumentedPublicProperty")
    public var onOverflow: BufferOverflow by config::onOverflow

    @FlowMVIDSL
    @Deprecated(ConfigDeprecation)
    @Suppress("UndocumentedPublicProperty")
    public var intentCapacity: Int by config::intentCapacity

    @FlowMVIDSL
    @Deprecated(ConfigDeprecation)
    @Suppress("UndocumentedPublicProperty")
    public var atomicStateUpdates: Boolean by config::atomicStateUpdates

    // endregion

    // region Plugins
    /**
     * Install [StorePlugin]s. See the other overload to build the plugin on the fly.
     * This installs prebuilt plugins.
     *
     * Plugins will **preserve** the order of installation and will proceed according to this order.
     * See [StorePlugin] for comprehensive information on the behavior of plugins.
     *
     * Installation of the same plugin multiple times is **not allowed**.
     * See [StorePlugin.name] for more info and solutions.
     */
    @FlowMVIDSL
    public infix fun install(plugins: Iterable<LazyPlugin<S, I, A>>): Unit = plugins.forEach {
        require(this.plugins.add(it)) { duplicatePluginMessage("plugin", it.toString()) }
    }

    /**
     * Install [StorePlugin]s. See the other overload to build the plugin on the fly.
     * This installs prebuilt plugins.
     *
     * Plugins will **preserve** the order of installation and will proceed according to this order.
     * See [StorePlugin] for comprehensive information on the behavior of plugins.
     *
     * Installation of the same plugin multiple times is **not allowed**.
     * See [StorePlugin.name] for more info and solutions.
     */
    @FlowMVIDSL
    public fun install(
        plugin: LazyPlugin<S, I, A>,
        vararg other: LazyPlugin<S, I, A>,
    ): Unit = install(sequenceOf(plugin).plus(other).asIterable())

    /**
     * Create and install a new [StorePlugin].
     * Please see documentation for the other overload for more details.
     * @see install
     */
    @FlowMVIDSL
    public inline infix fun install(
        crossinline block: LazyPluginBuilder<S, I, A>.() -> Unit
    ): Unit = install(lazyPlugin(block))

    /**
     * Alias for [install]
     */
    @FlowMVIDSL
    public fun LazyPlugin<S, I, A>.install(): Unit = install(this)

    // endregion

    // region Decorators

    @FlowMVIDSL
    @JvmName("decorate")
    @ExperimentalFlowMVIAPI
    public infix fun install(decorators: Iterable<PluginDecorator<S, I, A>>): Unit = decorators.forEach {
        require(this.decorators.add(it)) { duplicatePluginMessage("decorator", it.toString()) }
    }

    @FlowMVIDSL
    @JvmName("decorate")
    @ExperimentalFlowMVIAPI
    public fun install(decorator: PluginDecorator<S, I, A>, vararg other: PluginDecorator<S, I, A>) {
        install(sequenceOf(decorator).plus(other).asIterable())
    }

    @FlowMVIDSL
    @ExperimentalFlowMVIAPI
    public inline infix fun decorate(block: DecoratorBuilder<S, I, A>.() -> Unit): Unit = install(decorator(block))

    @FlowMVIDSL
    @JvmName("decorate")
    @ExperimentalFlowMVIAPI
    public fun PluginDecorator<S, I, A>.install(): Unit = install(this)

    // endregion

    // it's important to first convert the collection to an immutable before iterating, or the
    // iterator will throw
    @PublishedApi
    @FlowMVIDSL
    internal operator fun invoke(): Store<S, I, A> = config(initial).let { config ->
        StoreImpl(
            config = config,
            plugin = (plugins.map { it(config).asInstance() }.compose() decoratedWith decorators).asInstance()
        )
    }
}
