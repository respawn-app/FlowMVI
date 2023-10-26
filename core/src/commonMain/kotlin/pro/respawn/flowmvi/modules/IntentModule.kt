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
): IntentModule<I> = IntentModuleImpl(parallel, capacity, overflow)

private class IntentModuleImpl<I : MVIIntent>(
    private val parallel: Boolean,
    capacity: Int,
    overflow: BufferOverflow,
) : IntentModule<I> {

    private val intents = Channel<I>(capacity, overflow)

    override suspend fun emit(intent: I) = intents.send(intent)
    override fun intent(intent: I) {
        intents.trySend(intent)
    }

    override suspend fun awaitIntents(onIntent: suspend (intent: I) -> Unit) = coroutineScope {
        for (intent in intents) {
            // must always suspend the current scope to wait for intents
            if (parallel) launch {
                onIntent(intent)
            } else onIntent(intent)
            yield()
        }
    }
}
