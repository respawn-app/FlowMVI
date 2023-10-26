package pro.respawn.flowmvi.api

import androidx.compose.runtime.Stable

/**
 * An entity that can receive and process [MVIIntent]s. Usually, this is a [Store].
 */
@Stable
public interface IntentReceiver<in I : MVIIntent> {

    /**
     * Send an intent asynchronously. The intent is sent to the receiver and is placed in a queue.
     * When [IntentReceiver] is available (e.g. when the [Store] is started), the intent will be processed.
     * Intents that overflow the buffer will be handled according to the
     * behavior specified in [pro.respawn.flowmvi.dsl.StoreBuilder.onOverflow].
     * If the store is not started when an intent is sent, it will wait in the buffer, and **will be processed
     * once the store is started**.
     * @See MVIIntent
     */
    public fun send(intent: I): Unit = intent(intent)

    /**
     * Alias for [send] with one difference - this function will suspend if
     * [pro.respawn.flowmvi.dsl.StoreBuilder.onOverflow] permits it.
     */
    public suspend fun emit(intent: I)

    /**
     * Alias for [send]
     */
    public fun intent(intent: I)
}
