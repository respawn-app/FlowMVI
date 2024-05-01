package pro.respawn.flowmvi.essenty.dsl

import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.subscribe

/**
 * Subscribe to the [store] in the scope of this [LifecycleOwner]'s lifecycle.
 */
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> LifecycleOwner.subscribe(
    store: Store<S, I, A>,
    scope: CoroutineScope = coroutineScope(),
    crossinline block: suspend Provider<S, I, A>.() -> Unit,
): Job = scope.subscribe(store, consume = block)

/**
 * Subscribe to the [Container.store] of `this` container in the scope of `this` [LifecycleOwner]'s lifecycle.
 */
public inline fun <T, S : MVIState, I : MVIIntent, A : MVIAction> T.subscribe(
    scope: CoroutineScope = coroutineScope(),
    crossinline block: suspend Provider<S, I, A>.() -> Unit,
): Job where T : LifecycleOwner, T : Container<S, I, A> = subscribe(store, scope, block = block)
