package pro.respawn.flowmvi.api

public interface ShutdownContext<S : MVIState, I : MVIIntent, A : MVIAction> : ImmediateStateReceiver<S> {

    /**
     * The [StoreConfiguration] of this store
     */
    public val config: StoreConfiguration<S>
}
