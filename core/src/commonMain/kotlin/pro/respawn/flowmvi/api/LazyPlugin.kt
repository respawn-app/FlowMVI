package pro.respawn.flowmvi.api

/**
 * A plugin that is build lazily at the time of [Store] creation.
 */
public fun interface LazyPlugin<S : MVIState, I : MVIIntent, A : MVIAction> {

    /**
     * Create the [StorePlugin] using provided [config]
     */
    public operator fun invoke(config: StoreConfiguration<S>): StorePlugin<S, I, A>
}
