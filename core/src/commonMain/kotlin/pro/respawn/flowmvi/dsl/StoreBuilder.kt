@file:Suppress("MemberVisibilityCanBePrivate")

package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable.start
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.MutableStore
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.plugins.CompositePlugin
import pro.respawn.flowmvi.store.StoreImpl
import kotlin.jvm.JvmName

@FlowMVIDSL
public class StoreBuilder<S : MVIState, I : MVIIntent, A : MVIAction>(
    public val name: String,
    public val initial: S,
) {

    private var plugins: MutableSet<StorePlugin<S, I, A>> = mutableSetOf()

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
        plugins.add(plugin)
    }

    @FlowMVIDSL
    public fun install(
        name: String,
        plugin: StorePluginBuilder<S, I, A>.() -> Unit
    ): Unit = install(storePlugin(name, plugin))

    @PublishedApi
    @FlowMVIDSL
    internal fun build(): MutableStore<S, I, A> = StoreConfiguration(
        name = name,
        initial = initial,
        parallelIntents = parallelIntents,
        actionShareBehavior = actionShareBehavior,
        intentCapacity = intentCapacity,
        onOverflow = onOverflow,
        plugin = CompositePlugin(plugins = plugins.toSet()),
    ).let(::StoreImpl)
}

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> store(
    name: String,
    initial: S,
    configure: StoreBuilder<S, I, A>.() -> Unit,
): MutableStore<S, I, A> = StoreBuilder<S, I, A>(name, initial).run {
    configure()
    build()
}

@FlowMVIDSL
@JvmName("noActionStore")
public inline fun <S : MVIState, I : MVIIntent> store(
    name: String,
    initial: S,
    crossinline configure: StoreBuilder<S, I, Nothing>.() -> Unit
): MutableStore<S, I, Nothing> = store<S, I, Nothing>(name, initial) {
    actionShareBehavior = ActionShareBehavior.Disabled
    configure()
}

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyStore(
    name: String,
    initial: S,
    configure: StoreBuilder<S, I, A>.() -> Unit,
): Lazy<MutableStore<S, I, A>> = lazy { store(name, initial, configure) }

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyStore(
    name: String,
    scope: CoroutineScope,
    initial: S,
    configure: StoreBuilder<S, I, A>.() -> Unit,
): Lazy<MutableStore<S, I, A>> = lazy { store(name, initial, configure).apply { start(scope) } }
