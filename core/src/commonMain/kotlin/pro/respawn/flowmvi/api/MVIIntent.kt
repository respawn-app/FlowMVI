package pro.respawn.flowmvi.api

import androidx.compose.runtime.Immutable

/**
 * User interaction or other event that is sent to and processed by [IntentReceiver].
 *
 * **Must be immutable and comparable**. Most likely a data class or a data object.
 */
@Immutable
public interface MVIIntent {

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
