package pro.respawn.flowmvi.api

import androidx.compose.runtime.Immutable

/**
 * A single, one-shot, side-effect of processing an [MVIIntent], sent to [ActionConsumer],
 * processed by [ActionProvider] and handled by [ActionReceiver].
 *
 * Must be **immutable** and **comparable**, most likely a data class or a data object.
 */
@Immutable
public interface MVIAction {

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
