package pro.respawn.flowmvi.plugins

import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.storePlugin

public const val WhileSubscribedPluginName: String = "WhileSubscribed"

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.whileSubscribed(
    name: String = WhileSubscribedPluginName,
    firstSubscription: suspend PipelineContext<S, I, A>.() -> Unit,
): Unit = install(whileSubscribedPlugin(name, firstSubscription))

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> whileSubscribedPlugin(
    name: String = WhileSubscribedPluginName,
    crossinline firstSubscription: suspend PipelineContext<S, I, A>.() -> Unit,
): StorePlugin<S, I, A> = storePlugin(name) {
    onSubscribe {
        launch { firstSubscription() }
    }
}
