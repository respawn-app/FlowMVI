@file:Suppress("Filename")

package pro.respawn.flowmvi.android.view

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import pro.respawn.flowmvi.android.subscribe
import pro.respawn.flowmvi.api.Consumer
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store

/**
 *  Subscribe to the [store] lifecycle-aware. Call this in [Fragment.onViewCreated]
 *  @param consume called on each new action. Implement action handling here.
 *  @param render called each time the state changes. Render state here.
 *  @param lifecycleState the minimum lifecycle state the [LifecycleOwner] must be in to receive updates.
 *  @see repeatOnLifecycle
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> Fragment.subscribe(
    store: Store<S, I, A>,
    consume: (action: A) -> Unit,
    render: (state: S) -> Unit,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): Job = viewLifecycleOwner.subscribe(
    store = store,
    consume = consume,
    render = render,
    lifecycleState = lifecycleState
)

/**
 *  Subscribe to the store lifecycle-aware. Call this in [Fragment.onViewCreated]
 *  @param lifecycleState the minimum lifecycle state the [LifecycleOwner] must be in to receive updates.
 *  @see repeatOnLifecycle
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.subscribe(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): Job where T : Fragment, T : Consumer<S, I, A> =
    viewLifecycleOwner.subscribe(container.store, ::consume, ::render, lifecycleState)

/**
 * Subscribe to the store lifecycle-aware.
 * @param lifecycleState the minimum lifecycle state the [LifecycleOwner] must be in to receive updates.
 * @see repeatOnLifecycle
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.subscribe(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): Job where T : LifecycleOwner, T : Consumer<S, I, A> = subscribe(container.store, lifecycleState)
