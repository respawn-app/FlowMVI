package pro.respawn.flowmvi.plugins

import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Installs a plugin that invokes [block] when [pro.respawn.flowmvi.api.Store.start] is called.
 * @see StorePlugin.onStart
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.init(
    name: String? = null,
    @BuilderInference block: suspend PipelineContext<S, I, A>.() -> Unit
): Unit = install(initPlugin(name, block))

/**
 * Creates a plugin that invokes [block] after [pro.respawn.flowmvi.api.Store.start] is called.
 * @see StorePlugin.onStart
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> initPlugin(
    name: String? = null,
    @BuilderInference block: suspend PipelineContext<S, I, A>.() -> Unit,
): StorePlugin<S, I, A> = plugin {
    this.name = name
    onStart(block)
}

/**
 * [initPlugin] overload that launches a new coroutine instead of preventing store startup sequence.
 *
 * The [block] is executed on every startup of the store in a separate coroutine.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.asyncInit(
    context: CoroutineContext = EmptyCoroutineContext,
    name: String? = null,
    crossinline block: suspend PipelineContext<S, I, A>.() -> Unit
): Unit = init(name) { launch(context) { block() } }
