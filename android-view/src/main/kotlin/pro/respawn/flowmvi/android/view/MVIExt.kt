@file:Suppress("Filename")

package pro.respawn.flowmvi.android.view

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIProvider
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.MVIView
import pro.respawn.flowmvi.android.subscribe

/**
 *  Subscribe to the [provider] lifecycle-aware. Call this in [Fragment.onViewCreated]
 *  @param consume called on each new action. Implement action handling here.
 *  @param render called each time the state changes. Render state here.
 *  @param lifecycleState the minimum lifecycle state the [LifecycleOwner] must be in to receive updates.
 *  @see repeatOnLifecycle
 */
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> Fragment.subscribe(
    provider: MVIProvider<S, I, A>,
    crossinline consume: (action: A) -> Unit,
    crossinline render: (state: S) -> Unit,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): Job = viewLifecycleOwner.subscribe(provider, consume, render, lifecycleState)

/**
 *  Subscribe to the provider lifecycle-aware. Call this in [Fragment.onViewCreated]
 *  @param lifecycleState the minimum lifecycle state the [LifecycleOwner] must be in to receive updates.
 *  @see repeatOnLifecycle
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.subscribe(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): Job where T : Fragment, T : MVIView<S, I, A> =
    viewLifecycleOwner.subscribe(provider, ::consume, ::render, lifecycleState)
