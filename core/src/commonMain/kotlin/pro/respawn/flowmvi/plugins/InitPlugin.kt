package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.storePlugin

/**
 * Installs a plugin that invokes [block] in when [pro.respawn.flowmvi.api.Store.start] is called.
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.init(
    block: suspend PipelineContext<S, I, A>.() -> Unit
): Unit = install(initPlugin(block))

/**
 * Creates plugin that invokes [block] in when [pro.respawn.flowmvi.api.Store.start] is called.
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> initPlugin(
    block: suspend PipelineContext<S, I, A>.() -> Unit,
): StorePlugin<S, I, A> = storePlugin {
    onStart(block)
}
