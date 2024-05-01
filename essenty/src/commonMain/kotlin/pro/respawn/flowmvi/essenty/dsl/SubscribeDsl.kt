package pro.respawn.flowmvi.essenty.dsl

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.lifecycle.coroutines.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.ImmutableContainer
import pro.respawn.flowmvi.api.ImmutableStore
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.api.SubscriptionMode
import pro.respawn.flowmvi.dsl.collect
import pro.respawn.flowmvi.essenty.lifecycle.asEssentyLifecycle

/**
 * Subscribe to the [store] in the scope of this [LifecycleOwner]'s lifecycle.
 */
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> Lifecycle.subscribe(
    store: ImmutableStore<S, I, A>,
    scope: CoroutineScope,
    mode: SubscriptionMode = SubscriptionMode.Started,
    crossinline block: suspend Provider<S, I, A>.() -> Unit,
): Job = scope.launch {
    repeatOnLifecycle(mode.asEssentyLifecycle) {
        store.collect(block)
    }
}

/**
 * Subscribe to the [store] in the scope of this [LifecycleOwner]'s lifecycle.
 */
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> LifecycleOwner.subscribe(
    store: ImmutableStore<S, I, A>,
    scope: CoroutineScope = coroutineScope(),
    mode: SubscriptionMode = SubscriptionMode.Started,
    crossinline block: suspend Provider<S, I, A>.() -> Unit,
): Job = lifecycle.subscribe(store, scope, mode, block)

/**
 * Subscribe to the [Container.store] of `this` container in the scope of `this` [LifecycleOwner]'s lifecycle.
 *
 * The subscription will follow the [mode] specified.
 */
public inline fun <T, S : MVIState, I : MVIIntent, A : MVIAction> T.subscribe(
    scope: CoroutineScope = coroutineScope(),
    mode: SubscriptionMode = SubscriptionMode.Started,
    crossinline block: suspend Provider<S, I, A>.() -> Unit,
): Job where T : LifecycleOwner, T : ImmutableContainer<S, I, A> = subscribe(store, scope, mode, block)

/**
 * Subscribe to the [Container.store] of `this` container in the scope of `this` [LifecycleOwner]'s lifecycle.
 *
 * The subscription will follow the [mode] specified.
 */
public inline fun <T, S : MVIState, I : MVIIntent, A : MVIAction> T.subscribe(
    scope: CoroutineScope = coroutineScope(),
    mode: SubscriptionMode = SubscriptionMode.Started,
    crossinline block: suspend Provider<S, I, A>.() -> Unit,
): Job where T : LifecycleOwner, T : ImmutableStore<S, I, A> = subscribe(this, scope, mode, block)
