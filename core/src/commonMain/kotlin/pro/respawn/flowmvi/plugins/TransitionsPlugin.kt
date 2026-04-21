@file:pro.respawn.flowmvi.annotation.MustUseReturnValues

package pro.respawn.flowmvi.plugins

import kotlinx.atomicfu.atomic
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.TransitionsBuilder
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.exceptions.InvalidStateException

/**
 * Default name for [transitionsPlugin].
 * This is hardcoded so that multiple [transitions] invocations are not allowed w/o
 * explicit consent of the user as most often multiple FSM plugins will conflict with each other.
 * Provide your own name if you want to have multiple FSM plugins.
 */
public const val TransitionsPluginName: String = "TransitionsPlugin"

/**
 * Install a finite state machine plugin that handles intents based on the current state type.
 *
 * This is a full replacement for [reduce] — handlers have full `PipelineContext` access.
 * Intents not matched by any FSM handler pass through to downstream plugins.
 *
 * Name is hardcoded because usually multiple FSM plugins are not used.
 * Provide your own name if you want to have multiple FSM plugins.
 *
 * @see transitionsPlugin
 */
@IgnorableReturnValue
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.transitions(
    name: String = TransitionsPluginName,
    @BuilderInference block: TransitionsBuilder<S, I, A>.() -> Unit,
): Unit = install(transitionsPlugin(name, block))

/**
 * Create a finite state machine plugin that handles intents based on the current state type.
 *
 * The plugin uses `onIntent` to dispatch intents to type-matched handlers and `onState` to enforce
 * that cross-type state transitions only happen through FSM handlers.
 *
 * * In debug mode (`config.debuggable = true`), invalid external transitions throw [InvalidStateException].
 * * In release mode, invalid external transitions are silently vetoed (the old state is kept).
 * * Same-type state updates (e.g., `copy()` on a data class) are always allowed.
 * * Intents not matched by any handler pass through to downstream plugins.
 *
 * When `compose()` calls are present in the transitions DSL, the returned plugin is a composite of:
 * 1. A [childStorePlugin] that manages child store lifecycles
 * 2. A compose plugin that manages state subscriptions and merging
 * 3. The core FSM plugin with `onIntent` dispatch and `onState` enforcement
 *
 * @param name Plugin name. Defaults to [TransitionsPluginName].
 * @param block DSL block to define state transitions.
 * @see transitions
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> transitionsPlugin(
    name: String = TransitionsPluginName,
    @BuilderInference block: TransitionsBuilder<S, I, A>.() -> Unit,
): StorePlugin<S, I, A> {
    val builder = TransitionsBuilder<S, I, A>().apply(block)
    val graph = builder.build()
    val topLevelCompositions = builder.topLevelCompositions.toList()
    val scopedCompositions = graph.definitions.values.flatMap { it.compositions }

    val fsmPlugin = buildFsmPlugin(graph, name)

    if (topLevelCompositions.isEmpty() && scopedCompositions.isEmpty()) return fsmPlugin

    val allChildStores = (topLevelCompositions + scopedCompositions)
        .map { it.store }
        .toSet()

    val childPlugin = childStorePlugin<S, I, A>(allChildStores, force = null, blocking = false)
    val composePlugin = buildComposePlugin<S, I, A>(topLevelCompositions, scopedCompositions)

    return compositePlugin(
        plugins = listOfNotNull(childPlugin, composePlugin, fsmPlugin),
        name = name,
    )
}

private class HandlerDepthTracker {
    private val depth = atomic(0)
    fun increment() = depth.incrementAndGet()
    fun decrement() = depth.decrementAndGet()
    val isInsideHandler: Boolean get() = depth.value > 0
}

/**
 * Build the core FSM plugin with `onIntent` dispatch and `onState` enforcement.
 */
@OptIn(InternalFlowMVIAPI::class)
@PublishedApi
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> buildFsmPlugin(
    graph: TransitionGraph<S, I, A>,
    name: String,
): StorePlugin<S, I, A> {
    val tracker = HandlerDepthTracker()
    return plugin {
        this.name = name

        onIntent { intent ->
            val currentState = states.value
            val stateClass = currentState::class
            val definition = graph.definitions[stateClass]
                ?: return@onIntent intent
            val handler = definition.handlers[intent::class]
                ?: return@onIntent intent

            tracker.increment()
            try {
                handler.invoke(this, currentState, intent)
            } finally {
                tracker.decrement()
            }

            null // consume the intent
        }

        onState { old, new ->
            if (tracker.isInsideHandler) return@onState new
            if (old::class == new::class) return@onState new
            if (config.debuggable) {
                throw InvalidStateException(new::class.simpleName, old::class.simpleName)
            }
            old // veto silently in release mode
        }
    }
}
