package pro.respawn.flowmvi.api

/**
 * A simple class that delegates to the [store] property.
 */
public interface Container<S : MVIState, I : MVIIntent, A : MVIAction> {

    /**
     * A [Store] that is used with the container
     */
    public val store: Store<S, I, A>
}
