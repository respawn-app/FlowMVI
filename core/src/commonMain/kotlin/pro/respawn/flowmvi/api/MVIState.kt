package pro.respawn.flowmvi.api

import androidx.compose.runtime.Immutable

/**
 * The state of the [StateProvider], most likely a [Store].
 * States updates are sent to the [StateReceiver] and consumed by the [StateConsumer].
 *
 * The state must be **comparable** and **immutable** (most likely a data class or a data object).
 */
@Immutable
public interface MVIState {

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
