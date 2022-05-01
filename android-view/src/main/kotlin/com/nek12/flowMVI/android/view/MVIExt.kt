package com.nek12.flowMVI.android.view

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.nek12.flowMVI.MVIAction
import com.nek12.flowMVI.MVIIntent
import com.nek12.flowMVI.MVIProvider
import com.nek12.flowMVI.MVIState
import com.nek12.flowMVI.MVIView
import com.nek12.flowMVI.android.subscribe

/**
 * Call this in onViewCreated()
 *  @see repeatOnLifecycle
 */
inline fun <S: MVIState, I: MVIIntent, A: MVIAction> Fragment.subscribe(
    provider: MVIProvider<S, I, A>,
    crossinline consume: (action: A) -> Unit,
    crossinline render: (state: S) -> Unit,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
) = viewLifecycleOwner.subscribe(provider, consume, render, lifecycleState)

/**
 * Call this in onViewCreated()
 *  @see repeatOnLifecycle
 */
fun <S: MVIState, I: MVIIntent, A: MVIAction, T> T.subscribe(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
) where T: Fragment, T: MVIView<S, I, A> = viewLifecycleOwner.subscribe(provider, ::consume, ::render, lifecycleState)
