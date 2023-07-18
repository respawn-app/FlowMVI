package pro.respawn.flowmvi.api

/**
 * A single, one-shot, side-effect of processing an [MVIIntent], sent by [MVIProvider].
 * Consumed in the ui-layer as a one-time action.
 * Must be immutable.
 */
public interface MVIAction
