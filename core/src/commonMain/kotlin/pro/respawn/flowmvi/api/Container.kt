@file:Suppress("UnusedReceiverParameter")

package pro.respawn.flowmvi.api

import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.dsl.BuildStore

/**
 * A simple class that delegates to the [store] property.
 */
public interface Container<S : MVIState, I : MVIIntent, A : MVIAction> {

    /**
     * A [Store] that is used with the container
     */
    public val store: Store<S, I, A>
}

// these extensions are needed to auto-resolve types to the Container class types

/**
 * Alias for [pro.respawn.flowmvi.dsl.lazyStore] (with a scope)
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> Container<S, I, A>.lazyStore(
    initial: S,
    scope: CoroutineScope,
    configure: BuildStore<S, I, A>
): Lazy<Store<S, I, A>> = pro.respawn.flowmvi.dsl.lazyStore(initial, scope, configure)

/**
 * Alias for [pro.respawn.flowmvi.dsl.lazyStore]
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> Container<S, I, A>.lazyStore(
    initial: S,
    configure: BuildStore<S, I, A>
): Lazy<Store<S, I, A>> = pro.respawn.flowmvi.dsl.lazyStore(initial, configure)

/**
 * Alias for [pro.respawn.flowmvi.dsl.store]
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> Container<S, I, A>.store(
    initial: S,
    scope: CoroutineScope,
    configure: BuildStore<S, I, A>
): Store<S, I, A> = pro.respawn.flowmvi.dsl.store(initial, scope, configure)

/**
 * Alias for [pro.respawn.flowmvi.dsl.store]
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction> Container<S, I, A>.store(
    initial: S,
    configure: BuildStore<S, I, A>
): Store<S, I, A> = pro.respawn.flowmvi.dsl.store(initial, configure)
