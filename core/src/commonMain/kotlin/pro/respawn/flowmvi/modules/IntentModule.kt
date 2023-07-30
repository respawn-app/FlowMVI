package pro.respawn.flowmvi.modules

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIIntent

internal interface IntentModule<I : MVIIntent> : IntentReceiver<I> {

    suspend fun receive(): I
}

internal fun <I : MVIIntent> intentModule(
    capacity: Int,
    overflow: BufferOverflow,
): IntentModule<I> = IntentModuleImpl(capacity, overflow)

private class IntentModuleImpl<I : MVIIntent>(
    capacity: Int,
    overflow: BufferOverflow,
) : IntentModule<I> {

    private val intents = Channel<I>(capacity, overflow)

    override suspend fun receive(): I = intents.receive()

    override fun send(intent: I) {
        intents.trySend(intent)
    }

    override suspend fun emit(intent: I) = intents.send(intent)
}
