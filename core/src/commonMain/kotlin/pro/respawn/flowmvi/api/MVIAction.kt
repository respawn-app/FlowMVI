package pro.respawn.flowmvi.api

/**
 * A single, one-shot, side-effect of processing an [MVIIntent], sent by [Store].
 * Consumed in the ui-layer as a one-time action.
 * Must be **immutable** and **comparable**, most likely a data class or a data object.
 */
public interface MVIAction {

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
