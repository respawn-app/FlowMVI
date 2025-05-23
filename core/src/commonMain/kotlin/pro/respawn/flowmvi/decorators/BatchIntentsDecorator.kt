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
import pro.respawn.flowmvi.decorator.PluginDecorator
import pro.respawn.flowmvi.decorator.decorator
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.logging.info
import kotlin.time.Duration

/**
 * The mode of batching for the [batchIntentsDecorator].
 */
public sealed interface BatchingMode {

    /**
     * Batch intents until [size] is reached after which the intents will be flushed one-by-one in the order they
     * originally came.
     */
    public data class Amount(val size: Int) : BatchingMode

    /**
     * Batch intents for the [duration]. Each [duration] increment, all intents in the queue (if any) will be flushed.
     * This process starts when the first intent is received and store is started.
     * The intents will be flushed one-by-one in the order they originally came.
     */
    public data class Time(val duration: Duration) : BatchingMode
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

    internal fun push(intent: I) = _queue.update { it + intent }

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
 */
@ExperimentalFlowMVIAPI
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> batchIntentsDecorator(
    mode: BatchingMode,
    queue: BatchQueue<I> = BatchQueue(),
    name: String? = "BatchIntentsDecorator"
): PluginDecorator<S, I, A> = decorator {
    this.name = name
    var job: Job? = null
    if (mode is BatchingMode.Time) onStart { child ->
        with(child) {
            job = launch(start = CoroutineStart.LAZY) {
                while (isActive) {
                    delay(mode.duration)
                    val intents = queue.flush()
                    config.logger.info(name) { "Flushing ${intents.size} after batching for ${mode.duration}" }
                    intents.forEach { onIntent(it) }
                }
            }
            onStart()
        }
    }
    onIntent { child, intent ->
        with(child) {
            when (mode) {
                is BatchingMode.Time -> {
                    queue.push(intent)
                    if (job?.isActive != true) job?.start()
                    null
                }
                is BatchingMode.Amount -> {
                    queue.push(intent)
                    if (queue.queue.value.size <= mode.size) return@onIntent null
                    val intents = queue.flush()
                    config.logger.info(name) { "Flushing ${intents.size} after batching" }
                    // todo: onIntent invocation result ignored?
                    intents.forEach { onIntent(it) }
                    null
                }
            }
        }
    }
    onStop { child, e ->
        job?.cancel()
        job = null
        queue.flush()
        child.run { onStop(e) }
    }
}

/**
 * Installs a new [batchIntentsDecorator] for all plugins of this store.
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.batchIntents(
    mode: BatchingMode,
    name: String? = "BatchIntentsDecorator",
): BatchQueue<I> = BatchQueue<I>().also { install(batchIntentsDecorator(mode, it, name)) }
