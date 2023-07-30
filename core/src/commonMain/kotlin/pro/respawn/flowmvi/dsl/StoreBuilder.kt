@file:Suppress("MemberVisibilityCanBePrivate")

package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.MutableStore
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.plugins.CompositePlugin
import pro.respawn.flowmvi.store.StoreImpl
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName

@JvmInline
public value class Init<S : MVIState>(public val state: S)

public typealias Build<S, I, A> = StoreBuilder<S, I, A>.() -> Init<S>

@FlowMVIDSL
public class StoreBuilder<S : MVIState, I : MVIIntent, A : MVIAction> {

    private var plugins: MutableSet<StorePlugin<S, I, A>> = mutableSetOf()
    public var name: String? = null

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
    @BuilderInference crossinline configure: Build<S, I, A>,
): MutableStore<S, I, A> = StoreBuilder<S, I, A>().run {
    build(configure())
}

@FlowMVIDSL
@JvmName("noActionStore")
public inline fun <S : MVIState, I : MVIIntent> store(
    @BuilderInference crossinline configure: Build<S, I, Nothing>,
): MutableStore<S, I, Nothing> = store<S, I, Nothing> {
    actionShareBehavior = ActionShareBehavior.Disabled
    configure()
}

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyStore(
    @BuilderInference crossinline configure: Build<S, I, A>,
): Lazy<MutableStore<S, I, A>> = lazy { store(configure) }

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyStore(
    scope: CoroutineScope,
    @BuilderInference crossinline configure: Build<S, I, A>,
): Lazy<MutableStore<S, I, A>> = lazy { store(configure).apply { start(scope) } }

private fun duplicatePluginMessage(name: String) = """
    You have attempted to install plugin $name which was already installed.
    Plugins can be repeatable if they have different names or are different instances of the target class.
    You either have installed the same plugin instance twice or have installed two plugins with the same name.
    To fix, please either create a new plugin instance for each installation (when not using names) 
    or override the plugin name to be unique among all plugins for this store.
""".trimIndent()
