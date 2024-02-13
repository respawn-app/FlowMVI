package pro.respawn.flowmvi.api

/**
 * A single, one-shot, side-effect of processing an [MVIIntent], sent to [ActionConsumer],
 * processed by [ActionProvider] and handled by [ActionReceiver].
 *
 * Must be **immutable** and **comparable**, most likely a data class or a data object.
 */
public interface MVIAction {

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
