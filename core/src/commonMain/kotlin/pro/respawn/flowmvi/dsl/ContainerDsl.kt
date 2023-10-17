@file:Suppress("UnusedReceiverParameter")

package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store

// these extensions are needed to auto-resolve types to the Container class.

/**
 * Alias for [pro.respawn.flowmvi.dsl.lazyStore] (with a scope)
 */
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> Container<S, I, A>.lazyStore(
    initial: S,
    scope: CoroutineScope,
    @BuilderInference crossinline configure: BuildStore<S, I, A>
): Lazy<Store<S, I, A>> = pro.respawn.flowmvi.dsl.lazyStore(initial, scope, configure)

/**
 * Alias for [pro.respawn.flowmvi.dsl.lazyStore]
 */
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> Container<S, I, A>.lazyStore(
    initial: S,
    @BuilderInference crossinline configure: BuildStore<S, I, A>
): Lazy<Store<S, I, A>> = pro.respawn.flowmvi.dsl.lazyStore(initial, configure)

/**
 * Alias for [pro.respawn.flowmvi.dsl.store]
 */
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> Container<S, I, A>.store(
    initial: S,
    scope: CoroutineScope,
    @BuilderInference crossinline configure: BuildStore<S, I, A>
): Store<S, I, A> = pro.respawn.flowmvi.dsl.store(initial, scope, configure)

/**
 * Alias for [pro.respawn.flowmvi.dsl.store]
 */
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> Container<S, I, A>.store(
    initial: S,
    @BuilderInference crossinline configure: BuildStore<S, I, A>
): Store<S, I, A> = pro.respawn.flowmvi.dsl.store(initial, configure)
