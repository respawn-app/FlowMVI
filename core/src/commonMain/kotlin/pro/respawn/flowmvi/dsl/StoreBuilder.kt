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
public value class Init<S : MVIState>(public val state: S)

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
     */
    @FlowMVIDSL
    public var name: String? = null

    /**
     * Declare that intents must be processed in parallel.
     * All guarantees on the order of [MVIIntent]s will be lost.
     * Intents may still be dropped according to [onOverflow]
     */
    @FlowMVIDSL
    public var parallelIntents: Boolean = false

    @FlowMVIDSL
    public var actionShareBehavior: ActionShareBehavior = ActionShareBehavior.Distribute()

    @FlowMVIDSL
    public var onOverflow: BufferOverflow = BufferOverflow.DROP_OLDEST

    @FlowMVIDSL
    public var intentCapacity: Int = Channel.UNLIMITED

    @FlowMVIDSL
    public fun install(plugin: StorePlugin<S, I, A>) {
        require(plugins.add(plugin)) { duplicatePluginMessage(plugin.toString()) }
    }

    @FlowMVIDSL
    public fun install(
        block: StorePluginBuilder<S, I, A>.() -> Unit
    ): Unit = install(storePlugin(block))

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

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> store(
    @BuilderInference crossinline configure: BuildStore<S, I, A>,
): Store<S, I, A> = StoreBuilder<S, I, A>().run {
    build(configure())
}

@FlowMVIDSL
@JvmName("noActionStore")
public inline fun <S : MVIState, I : MVIIntent> store(
    @BuilderInference crossinline configure: BuildStore<S, I, Nothing>,
): Store<S, I, Nothing> = store<S, I, Nothing> {
    actionShareBehavior = ActionShareBehavior.Disabled
    configure()
}

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyStore(
    @BuilderInference crossinline configure: BuildStore<S, I, A>,
): Lazy<Store<S, I, A>> = lazy { store(configure) }

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
""".trimIndent()
