package pro.respawn.flowmvi.api

/**
 * A simple class that delegates to the [store] property.
 */
public interface Container<S : MVIState, I : MVIIntent, A : MVIAction> : ImmutableContainer<S, I, A> {

    /**
     * A [Store] that is used with the container
     */
    override val store: Store<S, I, A>
}
