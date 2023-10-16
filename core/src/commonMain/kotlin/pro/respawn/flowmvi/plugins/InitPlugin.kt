package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin

/**
 * Installs a plugin that invokes [block] when [pro.respawn.flowmvi.api.Store.start] is called.
 * @see StorePlugin.onStart
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.init(
    @BuilderInference noinline block: suspend PipelineContext<S, I, A>.() -> Unit
): Unit = install(initPlugin(block))

/**
 * Creates a plugin that invokes [block] after [pro.respawn.flowmvi.api.Store.start] is called.
 * @see StorePlugin.onStart
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> initPlugin(
    @BuilderInference noinline block: suspend PipelineContext<S, I, A>.() -> Unit,
): StorePlugin<S, I, A> = plugin { onStart(block) }
