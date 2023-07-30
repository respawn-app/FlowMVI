package pro.respawn.flowmvi.api

/**
 * An interface for entities that can [consume] [MVIAction]s.
 * This interface is mainly implemented by [Store]
 */
public fun interface ActionConsumer<in A : MVIAction> {

    /**
     * Consume a one-time side-effect emitted by [Provider].
     *
     * This function is called each time an [MVIAction] arrives, only once.
     * It may send intents under a promise that no loops will occur.
     * This function will behave differently based on [ActionShareBehavior] chosen for the store.
     * It may drop old intents, wait for buffer to be cleared, or throw an exception.
     */
    public fun consume(action: A)
}
