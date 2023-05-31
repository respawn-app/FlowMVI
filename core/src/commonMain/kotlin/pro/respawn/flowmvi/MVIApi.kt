package pro.respawn.flowmvi

/**
 * The state of the view / consumer.
 * The state must be comparable and immutable (most likely a data class)
 */
public interface MVIState

/**
 * User interaction or other event that happens in the UI layer.
 * Must be immutable.
 */
public interface MVIIntent

/**
 * A single, one-shot, side-effect of processing an [MVIIntent], sent by [MVIProvider].
 * Consumed in the ui-layer as a one-time action.
 * Must be immutable.
 */
public interface MVIAction
