package pro.respawn.flowmvi.plugins

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
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

@PublishedApi
internal class SubscriptionHolder {

    private val job = atomic<Job?>(null)
    suspend fun start(
        scope: CoroutineScope,
        block: suspend () -> Unit
    ) = job.getAndSet(scope.launch { block() })?.cancelAndJoin()

    fun cancel(e: CancellationException?) = job.getAndSet(null)?.cancel(e)

    suspend fun cancelAndJoin() = job.getAndSet(null)?.cancelAndJoin()

    // when a job has finished execution, we still want to consider it present to not relaunch on each new subscription
    val isActive get() = job.value != null
}

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
 * Create a new plugin that invokes [block] **each time** the subscriber count reaches [minSubscriptions].
 * Nothing is invoked when more subscribers than [minSubscriptions] appear, however, the block will be invoked again
 * if the subscriber count drops below [minSubscriptions] and then reaches the new value again.
 * The block will be canceled when the subscription count drops below [minSubscriptions].
 *
 * You can safely suspend inside [block] as it's invoked asynchronously,
 * but be aware that jobs launched inside [block] will be launched in the [PipelineContext] of the store, not the subscriber scope
 *
 * There is no guarantee that this will be invoked when a new subscriber appears
 * It may be so that a second subscriber appears before the first one disappears (due to the parallel nature of
 * coroutines). In that case, the [block] will continue instead of being canceled and relaunched.
 *
 * If you want to launch jobs in the scope of the [block], use [kotlinx.coroutines.coroutineScope].
 * @see StorePlugin.onSubscribe
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> whileSubscribedPlugin(
    name: String? = null,
    minSubscriptions: Int = 1,
    @BuilderInference crossinline block: suspend PipelineContext<S, I, A>.() -> Unit,
): StorePlugin<S, I, A> = plugin {
    require(minSubscriptions > 0) { "Minimum number of subscribers must be greater than 0" }
    this.name = name
    val job = SubscriptionHolder()
    onSubscribe { current ->
        when {
            current < minSubscriptions -> job.cancelAndJoin()
            job.isActive -> Unit // condition was already satisfied
            current >= minSubscriptions -> job.start(this) { block() }
        }
    }
    onUnsubscribe { current ->
        if (current < minSubscriptions) job.cancelAndJoin()
    }
    onStop { job.cancel(CancellationException(null, it)) }
}
