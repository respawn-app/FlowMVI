package pro.respawn.flowmvi

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import pro.respawn.flowmvi.base.IntentReceiver

internal interface IntentModule<in I : MVIIntent> : IntentReceiver<I> {

    suspend fun receive(): @UnsafeVariance I
}

internal fun <I : MVIIntent> intentModule(
    capacity: Int = Channel.UNLIMITED,
    overflow: BufferOverflow = BufferOverflow.SUSPEND
): IntentModule<I> = IntentModuleImpl(capacity, overflow)

private class IntentModuleImpl<I : MVIIntent>(
    capacity: Int = Channel.UNLIMITED,
    overflow: BufferOverflow = BufferOverflow.SUSPEND
) : IntentModule<I> {

    private val intents = Channel<I>(capacity, overflow)

    override suspend fun receive(): I = intents.receive()

    override fun send(intent: I) {
        intents.trySend(intent)
    }
}
