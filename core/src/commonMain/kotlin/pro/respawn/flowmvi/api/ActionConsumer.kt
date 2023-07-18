package pro.respawn.flowmvi.api

public fun interface ActionConsumer<in A : MVIAction> {


    /**
     * Consume a one-time side-effect emitted by [Provider].
     *
     * This function is called each time an [MVIAction] arrives.
     * This function may send intents under the promise that no loops will occur.
     */
    public fun consume(action: A)
}
