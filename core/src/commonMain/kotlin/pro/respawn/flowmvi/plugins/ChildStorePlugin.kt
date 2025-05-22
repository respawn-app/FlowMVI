package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder

private suspend fun PipelineContext<*, *, *>.children(
    stores: Iterable<Store<*, *, *>>,
    force: Boolean = config.debuggable,
    blocking: Boolean = false,
) = stores.forEach {
    val lifecycle = if (force || !it.isActive) it.start(this) else null
    if (blocking) lifecycle?.awaitStartup()
}

/**
 * Create a plugin that ties the lifecycle of [children] to the lifecycle of the parent Store.
 * The [children] will be started and stopped together with the parent store, synchronously if [blocking] is true.
 *
 * @param children The stores to launch
 * @param force Whether to force start the stores even if they are already active.
 *              If null, uses the debuggable flag from the store configuration to prevent crashes in production.
 * @param blocking Whether to wait for the child stores to start before continuing
 * @param name Optional name for the plugin
 * @return A store plugin that can be installed in the parent store
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> childStorePlugin(
    children: Iterable<Store<*, *, *>>,
    force: Boolean? = null,
    blocking: Boolean = false,
    name: String? = null,
): StorePlugin<S, I, A> = initPlugin(name) { children(children, force ?: config.debuggable, blocking) }

/**
 * Create and install a new [childStorePlugin].
 *
 */
@FlowMVIDSL
public fun StoreBuilder<*, *, *>.installChild(
    children: Iterable<Store<*, *, *>>,
    force: Boolean? = null,
    blocking: Boolean = false,
    name: String? = null,
): Unit = install(childStorePlugin(children, force, blocking, name))

/**
 * Create and install a new [childStorePlugin]
 *
 */
@FlowMVIDSL
public fun StoreBuilder<*, *, *>.installChild(
    first: Store<*, *, *>,
    vararg other: Store<*, *, *>,
    force: Boolean? = null,
    blocking: Boolean = false,
    name: String? = null,
): Unit = buildSet {
    add(first)
    addAll(elements = other)
}.let { installChild(it, force, blocking, name) }

/**
 * Alias for [childStorePlugin] installation with a single store and default settings.
 *
 * Usage:
 * ```kotlin
 * val child1 = store(Loading) { ... }
 *
 * val store = store(Loading) {
 *     this hasChild child1
 * }
 * ```
 *
 * See parent plugin for more details.
 *
 * @param other The store to install as a child
 */
@FlowMVIDSL
public infix fun StoreBuilder<*, *, *>.hasChild(other: Store<*, *, *>): Unit = installChild(setOf(other))
