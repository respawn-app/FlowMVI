package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.storePlugin

public const val ReducePluginName: String = "ReducePlugin"

/**
 * An operation that processes incoming [MVIIntent]s
 */
public typealias Reduce<S, I, A> = suspend PipelineContext<S, I, A>.(intent: I) -> Unit

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.reduce(
    name: String = ReducePluginName,
    reduce: Reduce<S, I, A>,
): Unit = install(reducePlugin(name, reduce))

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> reducePlugin(
    name: String = ReducePluginName,
    crossinline reduce: Reduce<S, I, A>,
): StorePlugin<S, I, A> = storePlugin(name) { onIntent { it.also { reduce(it) } } }
