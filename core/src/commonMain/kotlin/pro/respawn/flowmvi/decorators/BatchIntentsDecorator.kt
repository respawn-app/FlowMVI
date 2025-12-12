@file:MustUseReturnValues

package pro.respawn.flowmvi.decorators

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.decorator.PluginDecorator
import pro.respawn.flowmvi.decorator.decorator
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.logging.debug
import kotlin.time.Duration

/**
 * The mode of batching for the [batchIntentsDecorator].
 */
public sealed interface BatchingMode {

    /**
     * Batch intents until [size] is reached after which the intents will be flushed one-by-one in the order they
     * originally came.
     */
    public data class Amount(val size: Int) : BatchingMode {
        init {
            require(size > 0) { "Batch size must be > 0" }
        }
    }

    /**
     * Batch intents for the [duration]. Each [duration] increment, all intents in the queue (if any) will be flushed.
     * This process starts when the first intent is received and store is started.
     * The intents will be flushed one-by-one in the order they originally came.
     */
    public data class Time(val duration: Duration) : BatchingMode {
        init {
            require(duration.isPositive()) { "Batch duration must be > 0" }
        }
    }
}

/**
 * Queue holder of the [batchIntentsDecorator].
 *
 * Use [flush] to forcibly clear the queue. No intents will be sent in this case.
 *
 * Observe [queue] to get the current queue.
 */
public class BatchQueue<I : MVIIntent> {

    private val _queue = MutableStateFlow<List<I>>(emptyList())
    public val queue: StateFlow<List<I>> = _queue.asStateFlow()

    @IgnorableReturnValue
    internal fun push(intent: I) = _queue.update { it + intent }

    internal fun pushAndFlushIfReached(intent: I, size: Int): List<I> {
        var flushed = emptyList<I>()
        _queue.update { current ->
            if (current.size + 1 < size) current + intent else {
                flushed = current + intent
                emptyList()
            }
        }
        return flushed
    }

    /**
     * Forcibly clear the queue without sending any intents.
     *
     * @returns intents that were flushed.
     */
    public fun flush(): List<I> = _queue.getAndUpdate { emptyList() }
}

/**
 * A decorator that batches intents based on the [BatchingMode] and stores them in a [BatchQueue].
 *
 * Based on the mode, the intents will be batched either by time ("flush every N seconds") or amount.
 * See [BatchingMode] for details.
 *
 * @param onUnhandledIntent invoked for every intent that the child plugin returns instead of consuming.
 * In [BatchingMode.Amount] the callback fires for each unhandled item in the batch and the last one is also
 * returned from the decorator so the store can continue its regular undelivered flow.
 * In [BatchingMode.Time] flushing happens on a background job, therefore every unhandled item is forwarded
 * to this callback immediately and nothing is bubbled up.
 * By default the callback is a no-op, preserving the previous behaviour where unhandled intents were dropped.
 */
@ExperimentalFlowMVIAPI
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> batchIntentsDecorator(
    mode: BatchingMode,
    queue: BatchQueue<I> = BatchQueue(),
    name: String? = "BatchIntentsDecorator",
    onUnhandledIntent: suspend PipelineContext<S, I, A>.(intent: I) -> Unit = {}
): PluginDecorator<S, I, A> = decorator {
    this.name = name
    var job: Job? = null
    if (mode is BatchingMode.Time) onStart { child ->
        job = launch(start = CoroutineStart.LAZY) {
            while (isActive) {
                delay(mode.duration)
                val intents = queue.flush().ifEmpty { continue }
                config.logger.debug(name) {
                    "Flushing ${intents.size} after batching for ${mode.duration}"
                }
                val _ = intents.fold<I, I?>(null) { _, next ->
                    val result = child.run { onIntent(next) } ?: return@fold null
                    onUnhandledIntent(result)
                    result
                }
            }
        }
        child.run { onStart() }
    }
    onIntent { child, intent ->
        when (mode) {
            is BatchingMode.Time -> {
                queue.push(intent)
                if (job?.isActive != true) job?.start()
                null
            }
            is BatchingMode.Amount -> {
                val intents = queue.pushAndFlushIfReached(intent, mode.size)
                if (intents.isEmpty()) return@onIntent null
                config.logger.debug(name) { "Flushing ${intents.size} after batching" }
                intents.fold(null) { _, next ->
                    val result = child.run { onIntent(next) } ?: return@fold null
                    onUnhandledIntent(result)
                    result
                }
            }
        }
    }
    onStop { child, e ->
        job?.cancel()
        job = null
        child.run {
            queue.flush().forEach { intent -> onUndeliveredIntent(intent) }
            onStop(e)
        }
    }
}

/**
 * Installs a new [batchIntentsDecorator] for all plugins of this store.
 *
 * @param onUnhandledIntent see [batchIntentsDecorator] for semantics.
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.batchIntents(
    mode: BatchingMode,
    name: String? = "BatchIntentsDecorator",
    onUnhandledIntent: suspend PipelineContext<S, I, A>.(intent: I) -> Unit = {}
): BatchQueue<I> = BatchQueue<I>().also {
    install(batchIntentsDecorator(mode, it, name, onUnhandledIntent))
}
