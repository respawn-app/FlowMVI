package pro.respawn.flowmvi.android

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.ActionConsumer
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StateConsumer
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.subscribe
import kotlin.jvm.JvmName

/**
 *  Subscribe to the [store] lifecycle-aware.
 *  @param consume called on each new action. Implement action handling here.
 *  @param render called each time the state changes. Render state here.
 *  @param lifecycleState the minimum lifecycle state the [LifecycleOwner] must be in to receive updates.
 *  @see repeatOnLifecycle
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> LifecycleOwner.subscribe(
    store: Store<S, I, A>,
    noinline consume: suspend (action: A) -> Unit,
    crossinline render: suspend (state: S) -> Unit,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): Job = lifecycleScope.launch(Dispatchers.Main.immediate) {
    // https://github.com/Kotlin/kotlinx.coroutines/issues/2886
    // TL;DR: uses immediate dispatcher to circumvent prompt cancellation fallacy (and missed events)
    repeatOnLifecycle(lifecycleState) {
        this@repeatOnLifecycle.subscribe(store, consume, render)
    }
}

/**
 *  Subscribe to the [store] lifecycle-aware.
 *  @param render called each time the state changes. Render state here.
 *  @param lifecycleState the minimum lifecycle state the [LifecycleOwner] must be in to receive updates.
 *  @see repeatOnLifecycle
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> LifecycleOwner.subscribe(
    store: Store<S, I, A>,
    crossinline render: suspend (state: S) -> Unit,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): Job = lifecycleScope.launch(Dispatchers.Main.immediate) {
    // https://github.com/Kotlin/kotlinx.coroutines/issues/2886
    // TL;DR: uses immediate dispatcher to circumvent prompt cancellation fallacy (and missed events)
    repeatOnLifecycle(lifecycleState) {
        this@repeatOnLifecycle.subscribe(store, render)
    }
}

/**
 * Subscribe to the store lifecycle-aware.
 * @param lifecycleState the minimum lifecycle state the [LifecycleOwner] must be in to receive updates.
 * @see repeatOnLifecycle
 */
@JvmName("subscribeAndConsume")
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.subscribe(
    provider: Store<S, I, A>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): Job where T : LifecycleOwner, T : StateConsumer<S>, T : ActionConsumer<A> =
    subscribe(provider, ::consume, ::render, lifecycleState)

/**
 * Subscribe to the store lifecycle-aware. This function will not collect the store's actions.
 * @param lifecycleState the minimum lifecycle state the [LifecycleOwner] must be in to receive updates.
 * @see repeatOnLifecycle
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.subscribe(
    provider: Store<S, I, A>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): Job where T : LifecycleOwner, T : StateConsumer<S> =
    subscribe(provider, ::render, lifecycleState)
