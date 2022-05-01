package com.nek12.flowMVI.android

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nek12.flowMVI.MVIAction
import com.nek12.flowMVI.MVIIntent
import com.nek12.flowMVI.MVIProvider
import com.nek12.flowMVI.MVIState
import com.nek12.flowMVI.MVIView
import kotlinx.coroutines.launch

/**
 * Call this when you are using a component with defined lifecycle i.e. LifecycleService
 *  @see repeatOnLifecycle
 */
inline fun <S: MVIState, I: MVIIntent, A: MVIAction> LifecycleOwner.subscribe(
    provider: MVIProvider<S, I, A>,
    crossinline consume: (action: A) -> Unit,
    crossinline render: (state: S) -> Unit,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
) = lifecycleScope.launch {

    launch {
        repeatOnLifecycle(lifecycleState) {
            provider.states.collect { render(it) }
        }
    }

    launch {
        repeatOnLifecycle(lifecycleState) {
            provider.actions.collect { consume(it) }
        }
    }
}

/**
 * Call this whenever the lifecycle is first ready to accept new events
 * @see repeatOnLifecycle
 */
fun <S: MVIState, I: MVIIntent, A: MVIAction, T> T.subscribe(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
) where T: LifecycleOwner, T: MVIView<S, I, A> = subscribe(provider, ::consume, ::render, lifecycleState)
