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

// TODO: Maybe a better solution would be to keep the block running while any subscriber is present.

/**
 * Create and install a new [whileSubscribed] plugin. See the parent's function docs for more info.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.whileSubscribed(
    name: String? = null,
    crossinline onFirstSubscription: suspend PipelineContext<S, I, A>.() -> Unit,
): Unit = install(whileSubscribedPlugin(name, onFirstSubscription))

/**
 *  Create a new plugin that invokes [onFirstSubscription] the **first time** the store is being subscribed to.
 * Nothing is invoked when a second and next subscribers appear, however, the block will be invoked again
 * if the first subscriber appears again.
 * The block will be canceled when **the first subscriber unsubscribes** i.e the one that triggered the invocation.
 * You can safely suspend inside [onFirstSubscription] as it's invoked asynchronously.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> whileSubscribedPlugin(
    name: String? = null,
    crossinline onFirstSubscription: suspend PipelineContext<S, I, A>.() -> Unit,
): StorePlugin<S, I, A> = storePlugin {
    this.name = name
    onSubscribe { subscribers ->
        if (subscribers == 0) launch { onFirstSubscription() }
    }
}
