package pro.respawn.flowmvi.api

/**
 * The state of the subscriber.
 * The state must be **comparable** and **immutable** (most likely a data class or a data object).
 */
public interface MVIState {

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
