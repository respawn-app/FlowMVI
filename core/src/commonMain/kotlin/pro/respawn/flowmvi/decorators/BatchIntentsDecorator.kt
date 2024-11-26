package pro.respawn.flowmvi.decorators

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
import pro.respawn.flowmvi.decorator.StoreDecorator
import pro.respawn.flowmvi.decorator.decorator
import pro.respawn.flowmvi.decorator.ignore
import pro.respawn.flowmvi.decorator.proceed
import pro.respawn.flowmvi.logging.info
import kotlin.time.Duration

public sealed interface BatchingMode {
    public data class Amount(val maxSize: Int) : BatchingMode
    public data class Time(val flushEvery: Duration) : BatchingMode
}

public class BatchQueue<I : MVIIntent> {

    private val _queue = MutableStateFlow<List<I>>(emptyList())
    public val queue: StateFlow<List<I>> = _queue.asStateFlow()

    internal fun onIntent(intent: I) = _queue.update { it + intent }

    public fun flush(): List<I> = _queue.getAndUpdate { emptyList() }
}

@ExperimentalFlowMVIAPI
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> batchIntentsDecorator(
    mode: BatchingMode,
    queue: BatchQueue<I> = BatchQueue(),
): StoreDecorator<S, I, A> = decorator {
    if (mode is BatchingMode.Time) onStart {
        launch {
            while (isActive) {
                delay(mode.flushEvery)
                val intents = queue.flush()
                config.logger.info { "Flushing ${intents.size} after batching for ${mode.flushEvery}" }
                intents.forEach { onIntent(it) }
            }
        }
        proceed()
    }
    onIntent { intent ->
        when (mode) {
            is BatchingMode.Time -> queue.onIntent(intent)
            is BatchingMode.Amount -> {
                queue.onIntent(intent)
                if (queue.queue.value.size <= mode.maxSize) return@onIntent ignore()
                val intents = queue.flush()
                config.logger.info { "Flushing ${intents.size} after batching" }
                intents.forEach { proceed(it) }
            }
        }
        null
    }
}
