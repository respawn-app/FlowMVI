package com.nek12.flowMVI.android

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nek12.flowMVI.*
import kotlinx.coroutines.launch

/**
 * Call this when you are using a component with defined lifecycle i.e. LifecycleService
 *  @see repeatOnLifecycle
 */
inline fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIProvider<S, I, A>.subscribe(
    lifecycleOwner: LifecycleOwner,
    crossinline consume: (action: A) -> Unit,
    crossinline render: (state: S) -> Unit,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
) = with(lifecycleOwner) {
    lifecycleScope.launch {

        launch {
            repeatOnLifecycle(lifecycleState) {
                states.collect { render(it) }
            }
        }

        launch {
            repeatOnLifecycle(lifecycleState) {
                actions.collect { consume(it) }
            }
        }
    }
}

/**
 * Call this in onViewCreated()
 *  @see repeatOnLifecycle
 */
inline fun <S : MVIState, I : MVIIntent, A : MVIAction> Fragment.subscribe(
    provider: MVIProvider<S, I, A>,
    crossinline consume: (action: A) -> Unit,
    crossinline render: (state: S) -> Unit,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
) = provider.subscribe(viewLifecycleOwner, consume, render, lifecycleState)

/**
 * Call this in onViewCreated()
 *  @see repeatOnLifecycle
 */
fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.subscribe(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
) where T : Fragment, T : MVIView<S, I, A> = provider.subscribe(viewLifecycleOwner, ::consume, ::render, lifecycleState)

/**
 * Call this in onCreate()
 *  @see repeatOnLifecycle
 */
inline fun <S : MVIState, I : MVIIntent, A : MVIAction> ComponentActivity.subscribe(
    provider: MVIProvider<S, I, A>,
    crossinline consume: (action: A) -> Unit,
    crossinline render: (state: S) -> Unit,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
) = provider.subscribe(this, consume, render, lifecycleState)

/**
 * Call this whenever the lifecycle is first ready to accept new events
 * @see repeatOnLifecycle
 */
fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.subscribe(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
) where T : LifecycleOwner, T : MVIView<S, I, A> = provider.subscribe(this, ::consume, ::render, lifecycleState)
