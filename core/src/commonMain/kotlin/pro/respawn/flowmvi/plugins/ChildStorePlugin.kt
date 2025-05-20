package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder

// name variants:

// - source store
// - delegate store
// - child store
// - required store

private suspend fun PipelineContext<*, *, *>.installChild(
    stores: Iterable<Store<*, *, *>>,
    force: Boolean = config.debuggable,
    blocking: Boolean = false,
) = stores.forEach {
    val lifecycle = if (force || !it.isActive) it.start(this) else null
    if (blocking) lifecycle?.awaitStartup()
}

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> childStorePlugin(
    children: Iterable<Store<*, *, *>>,
    force: Boolean? = null,
    blocking: Boolean = false,
    name: String? = null,
): StorePlugin<S, I, A> = initPlugin(name) { installChild(children, force ?: config.debuggable, blocking) }

@FlowMVIDSL
public fun StoreBuilder<*, *, *>.installChild(
    children: Iterable<Store<*, *, *>>,
    force: Boolean? = null,
    blocking: Boolean = false,
    name: String? = null,
) = init(name) { installChild(children, force ?: config.debuggable, blocking) }

@FlowMVIDSL
public fun StoreBuilder<*, *, *>.installChild(
    first: Store<*, *, *>,
    vararg other: Store<*, *, *>,
    force: Boolean? = null,
    blocking: Boolean = false,
    name: String? = null,
) = buildSet {
    add(first)
    addAll(elements = other)
}.let { installChild(it, force, blocking, name) }

@FlowMVIDSL
public infix fun StoreBuilder<*, *, *>.hasChild(other: Store<*, *, *>) = installChild(setOf(other))
