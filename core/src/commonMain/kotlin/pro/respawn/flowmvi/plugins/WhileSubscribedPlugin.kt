package pro.respawn.flowmvi.plugins

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
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
    minSubscriptions: Int = 1,
    @BuilderInference crossinline block: suspend PipelineContext<S, I, A>.() -> Unit,
): Unit = install(whileSubscribedPlugin(name, minSubscriptions, block))

/**
 * Create a new plugin that invokes [block] the **first time** the subscriber count reaches [minSubscriptions].
 * Nothing is invoked when more subscribers appear, however, the block will be invoked again
 * if the subscriber count drops below [minSubscriptions] and then reaches the new value again.
 * The block will be canceled when the subscription count drops below [minSubscriptions].
 *
 * You can safely suspend inside [block] as it's invoked asynchronously,
 * but be aware that jobs launched inside [block] will be launched in the [PipelineContext] of the store, not the subscriber scope
 *
 * If you want to launch jobs in the scope of the subscription lifecycle, use [kotlinx.coroutines.coroutineScope].
 * @see StorePlugin.onSubscribe
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> whileSubscribedPlugin(
    name: String? = null,
    minSubscriptions: Int = 1,
    @BuilderInference crossinline block: suspend PipelineContext<S, I, A>.() -> Unit,
): StorePlugin<S, I, A> = plugin {
    this.name = name
    val job = atomic<Job?>(null)
    onSubscribe { previous ->
        val newSubscribers = previous + 1
        when {
            job.value?.isActive == true -> return@onSubscribe // condition was already satisfied
            newSubscribers >= minSubscriptions -> job.getAndSet(launch { block() })?.cancelAndJoin()
        }
    }
    onUnsubscribe { current ->
        if (current < minSubscriptions) job.getAndSet(null)?.cancelAndJoin()
    }
}
