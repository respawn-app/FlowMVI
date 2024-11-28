package pro.respawn.flowmvi.modules

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIIntent

internal interface IntentModule<I : MVIIntent> : IntentReceiver<I> {

    suspend fun awaitIntents(onIntent: suspend (intent: I) -> Unit)
}

internal fun <I : MVIIntent> intentModule(
    parallel: Boolean,
    capacity: Int,
    overflow: BufferOverflow,
    onUndeliveredIntent: ((intent: I) -> Unit)?,
): IntentModule<I> = when {
    !parallel -> SequentialChannelIntentModule(capacity, overflow, onUndeliveredIntent)
    else -> ParallelChannelIntentModule(capacity, overflow, onUndeliveredIntent)
}

private abstract class ChannelIntentModule<I : MVIIntent>(
    capacity: Int,
    overflow: BufferOverflow,
    onUndeliveredIntent: ((intent: I) -> Unit)?,
) : IntentModule<I> {

    val intents = Channel(capacity, overflow, onUndeliveredIntent)

    override suspend fun emit(intent: I) = intents.send(intent)
    override fun intent(intent: I) {
        intents.trySend(intent)
    }
}

private class SequentialChannelIntentModule<I : MVIIntent>(
    capacity: Int,
    overflow: BufferOverflow,
    onUndeliveredIntent: ((intent: I) -> Unit)?,
) : ChannelIntentModule<I>(capacity, overflow, onUndeliveredIntent) {

    override suspend fun awaitIntents(onIntent: suspend (intent: I) -> Unit) = coroutineScope {
        // must always suspend the current scope to wait for intents
        for (intent in intents) {
            onIntent(intent)
            yield() // TODO: Accounts for 50% performance loss, way to get rid of? Why needed?
        }
    }
}

private class ParallelChannelIntentModule<I : MVIIntent>(
    capacity: Int,
    overflow: BufferOverflow,
    onUndeliveredIntent: ((intent: I) -> Unit)?,
) : IntentModule<I> {

    private val intents = Channel(capacity, overflow, onUndeliveredIntent)

    override suspend fun emit(intent: I) = intents.send(intent)
    override fun intent(intent: I) {
        intents.trySend(intent)
    }

    // TODO: We should let the user limit parallelism here to avoid starvation
    override suspend fun awaitIntents(onIntent: suspend (intent: I) -> Unit) = coroutineScope {
        // must always suspend the current scope to wait for intents
        for (intent in intents) launch { onIntent(intent) }
    }
}
