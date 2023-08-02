package pro.respawn.flowmvi.plugins

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin

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
 * Nothing is invoked when more subscribers appear, however, the block will be invoked again
 * if the first subscriber appears again.
 * The block will be canceled when the store is closed, **not** when the subscriber disappears.
 * You can safely suspend inside [onFirstSubscription] as it's invoked asynchronously.
 * @see StorePlugin.onSubscribe
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> whileSubscribedPlugin(
    name: String? = null,
    crossinline onFirstSubscription: suspend PipelineContext<S, I, A>.() -> Unit,
): StorePlugin<S, I, A> = plugin {
    this.name = name
    var job by atomic<Job?>(null)
    onSubscribe { _, subscribers ->
        if (subscribers == 0) {
            job = launch { onFirstSubscription() }
        }
    }
    onUnsubscribe { subscribers ->
        if (subscribers == 0) job?.cancel()
    }
}
