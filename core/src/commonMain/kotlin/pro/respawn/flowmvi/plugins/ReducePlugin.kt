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
 * Default name for [reducePlugin].
 * This is hardcoded so that multiple [reduce] invocations are not allowed w/o
 * explicit consent of the user as most often multiple reducers are not used.
 * Provide your own name if you want to have multiple reducers.
 */
public const val ReducePluginName: String = "ReducePlugin"

/**
 * An operation that processes incoming [MVIIntent]s
 */
public typealias Reduce<S, I, A> = suspend PipelineContext<S, I, A>.(intent: I) -> Unit

/**
 * Create and install a [reducePlugin].
 * Name is hardcoded because usually multiple reducers are not used.
 * Provide your own name if you want to have multiple reducers.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.reduce(
    name: String = ReducePluginName,
    crossinline reduce: Reduce<S, I, A>,
): Unit = install(reducePlugin(name, reduce))

/**
 * Create  a new plugin that simply invokes [StorePlugin.onIntent], processes it and does not change the intent.
 * To change the intent, either create your own [storePlugin] or use [PipelineContext] to manage the store.
 * Name is hardcoded because usually multiple reducers are not used.
 * Provide your own name if you want to have multiple reducers.
 **/
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> reducePlugin(
    name: String = ReducePluginName,
    crossinline reduce: Reduce<S, I, A>,
): StorePlugin<S, I, A> = storePlugin {
    this.name = name
    onIntent { it.also { reduce(it) } }
}
