package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.util.whileSubscribed
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Create a new plugin that invokes [block] **each time** the subscriber count reaches [minSubscriptions].
 * Nothing is invoked when more subscribers than [minSubscriptions] appear, however, the block will be invoked again
 * if the subscriber count drops below [minSubscriptions] and then reaches the new value again.
 * The block will be canceled when the subscription count drops below [minSubscriptions].
 *
 * You are expected to suspend inside [block] as it's invoked asynchronously,
 * because jobs launched inside [block] will be launched in the [PipelineContext] of the store, not the subscription
 * scope. If you want to launch jobs in the scope of the [block], use [kotlinx.coroutines.coroutineScope].
 *
 * There is no guarantee that this will be invoked when a new subscriber appears
 * It may be so that a second subscriber appears before the first one disappears (due to the parallel nature of
 * coroutines). In that case, the [block] will continue instead of being canceled and relaunched.
 *
 * The [stopDelay] governs how long the block will stay active after the subscriber count drops below [minSubscriptions]
 * This is useful for platforms like Android, where a configuration change may briefly cause unsubscription.
 * By default, a small delay is introduced.
 *
 * @see StorePlugin.onSubscribe
 */
@OptIn(ExperimentalFlowMVIAPI::class)
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> whileSubscribedPlugin(
    name: String? = null,
    minSubscriptions: Int = 1,
    stopDelay: Duration = 1.seconds,
    @BuilderInference crossinline block: suspend PipelineContext<S, I, A>.() -> Unit,
): StorePlugin<S, I, A> = plugin {
    require(minSubscriptions > 0) { "Minimum number of subscribers must be greater than 0, got: $minSubscriptions" }
    require(stopDelay.isFinite() && !stopDelay.isNegative()) { "stopDelay must be non-negative, got: $stopDelay" }
    this.name = name
    onStart { whileSubscribed(stopDelay, minSubscriptions) { block() } }
}

/**
 * Create and install a new [whileSubscribedPlugin]. See the parent's function docs for more info.
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.whileSubscribed(
    name: String? = null,
    minSubscriptions: Int = 1,
    stopDelay: Duration = 1.seconds,
    @BuilderInference crossinline block: suspend PipelineContext<S, I, A>.() -> Unit,
): Unit = install(whileSubscribedPlugin(name, minSubscriptions, stopDelay, block))
