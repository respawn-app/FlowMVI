package pro.respawn.flowmvi.api

/**
 * User interaction or other event that is sent to and processed by [IntentReceiver].
 *
 * **Must be immutable and comparable**. Most likely a data class or a data object.
 */
public interface MVIIntent {

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
