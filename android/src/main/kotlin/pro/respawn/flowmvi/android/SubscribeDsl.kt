package pro.respawn.flowmvi.android

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.MVIView
import pro.respawn.flowmvi.api.ActionConsumer
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StateConsumer
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.subscribe

/**
 *  Subscribe to the [provider] lifecycle-aware.
 *  @param consume called on each new action. Implement action handling here.
 *  @param render called each time the state changes. Render state here.
 *  @param lifecycleState the minimum lifecycle state the [LifecycleOwner] must be in to receive updates.
 *  @see repeatOnLifecycle
 */

@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> LifecycleOwner.subscribe(
    store: Store<S, I, A>,
    crossinline consume: suspend (action: A) -> Unit,
    crossinline render: suspend (state: S) -> Unit,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): Job = lifecycleScope.launch(Dispatchers.Main.immediate) {
    // https://github.com/Kotlin/kotlinx.coroutines/issues/2886
    // TL;DR: uses immediate dispatcher to circumvent prompt cancellation fallacy (and missed events)
    repeatOnLifecycle(lifecycleState) {
        subscribe(store, consume, render)
    }
}

/**
 * Subscribe to the store lifecycle-aware.
 * @param lifecycleState the minimum lifecycle state the [LifecycleOwner] must be in to receive updates.
 * @see repeatOnLifecycle
 */

@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.subscribe(
    provider: Store<S, I, A>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): Job where T : LifecycleOwner, T : StateConsumer<S>, T : ActionConsumer<A> =
    subscribe(provider, ::consume, ::render, lifecycleState)

/**
 * Subscribe to the store lifecycle-aware.
 * @param lifecycleState the minimum lifecycle state the [LifecycleOwner] must be in to receive updates.
 * @see repeatOnLifecycle
 */

@FlowMVIDSL
@Deprecated(
    "Use the new MVIView interface version of the function",
    ReplaceWith("subscribe(lifecycleState)", "pro.respawn.flowmvi.android.view.MVIView")
)
public fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.subscribe(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): Job where T : LifecycleOwner, T : MVIView<S, I, A> = subscribe(provider, lifecycleState)
