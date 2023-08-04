package pro.respawn.flowmvi.api

import pro.respawn.flowmvi.dsl.BuildStore

/**
 * A simple class that delegates to the [store] property.
 */
public interface Container<S : MVIState, I : MVIIntent, A : MVIAction> {

    public val store: Store<S, I, A>

    public fun store(initial: S, build: BuildStore<S, I, A>): Store<S, I, A> =
        pro.respawn.flowmvi.dsl.store(initial, build)
}
