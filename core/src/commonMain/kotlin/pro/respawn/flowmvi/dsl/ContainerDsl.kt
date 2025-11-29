@file:Suppress("UnusedReceiverParameter")

@file:MustUseReturnValues

package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.ImmutableContainer
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store

// these extensions are needed to auto-resolve types to the Container class.

/**
 * Alias for [lazyStore] (with a scope)
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableContainer<S, I, A>.lazyStore(
    initial: S,
    scope: CoroutineScope,
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    @BuilderInference crossinline configure: BuildStore<S, I, A>
): Lazy<Store<S, I, A>> = pro.respawn.flowmvi.dsl.lazyStore(initial, scope, mode, configure)

/**
 * Alias for [lazyStore]
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableContainer<S, I, A>.lazyStore(
    initial: S,
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    @BuilderInference crossinline configure: BuildStore<S, I, A>
): Lazy<Store<S, I, A>> = pro.respawn.flowmvi.dsl.lazyStore(initial = initial, mode = mode, configure = configure)

/**
 * Alias for [store]
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableContainer<S, I, A>.store(
    initial: S,
    scope: CoroutineScope,
    @BuilderInference crossinline configure: BuildStore<S, I, A>
): Store<S, I, A> = pro.respawn.flowmvi.dsl.store(initial, scope, configure)

/**
 * Alias for [store]
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableContainer<S, I, A>.store(
    initial: S,
    @BuilderInference crossinline configure: BuildStore<S, I, A>
): Store<S, I, A> = pro.respawn.flowmvi.dsl.store(initial, configure)

/**
 * Alias for [Store.intent]
 */
@IgnorableReturnValue
public inline fun <I : MVIIntent> Container<*, I, *>.intent(
    first: I,
    vararg other: I
): Unit = store.intent(first, other = other)

/**
 * Alias for [Store.emit]
 */
@IgnorableReturnValue
public suspend inline fun <I : MVIIntent> Container<*, I, *>.emit(
    first: I,
    vararg other: I
): Unit = store.emit(first, other = other)
