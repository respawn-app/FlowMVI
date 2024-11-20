package pro.respawn.flowmvi.modules

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIIntent

internal fun <I : MVIIntent> intentModule(
    parallel: Boolean,
    capacity: Int,
    overflow: BufferOverflow,
    onUndeliveredIntent: ((intent: I) -> Unit)?,
): IntentModule<I> = IntentModuleImpl(parallel, capacity, overflow, onUndeliveredIntent)

internal interface IntentModule<I : MVIIntent> : IntentReceiver<I> {

    suspend fun awaitIntents(onIntent: suspend (intent: I) -> Unit)
}

private class IntentModuleImpl<I : MVIIntent>(
    private val parallel: Boolean,
    capacity: Int,
    overflow: BufferOverflow,
    onUndeliveredIntent: ((intent: I) -> Unit)?,
) : IntentModule<I> {

    private val intents = Channel(
        capacity,
        overflow,
        onUndeliveredIntent,
    )

    override suspend fun emit(intent: I) = intents.send(intent)
    override fun intent(intent: I) {
        intents.trySend(intent)
    }

    override suspend fun awaitIntents(onIntent: suspend (intent: I) -> Unit) = coroutineScope {
        // must always suspend the current scope to wait for intents
        for (intent in intents) {
            if (parallel) launch { onIntent(intent) } else onIntent(intent)
            yield()
        }
    }
}
