package pro.respawn.flowmvi.api

/**
 * User interaction or other event that happens in the UI layer.
 * Must be immutable.
 */
public interface MVIIntent {

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
