@file:Suppress("Filename")

package com.nek12.flowMVI.android

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nek12.flowMVI.MVIAction
import com.nek12.flowMVI.MVIIntent
import com.nek12.flowMVI.MVIProvider
import com.nek12.flowMVI.MVIState
import com.nek12.flowMVI.MVISubscriber
import com.nek12.flowMVI.MVIView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *  Subscribe to the [provider] lifecycle-aware.
 *  @param consume called on each new action. Implement action handling here.
 *  @param render called each time the state changes. Render state here.
 *  @param lifecycleState the minimum lifecycle state the [LifecycleOwner] must be in to receive updates.
 *  @see repeatOnLifecycle
 */
inline fun <S : MVIState, I : MVIIntent, A : MVIAction> LifecycleOwner.subscribe(
    provider: MVIProvider<S, I, A>,
    crossinline consume: (action: A) -> Unit,
    crossinline render: (state: S) -> Unit,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
) = lifecycleScope.launch {

    // using multiple repeatOnLifecycle instead of flowWithLifecycle to avoid creating hot flows

    // https://github.com/Kotlin/kotlinx.coroutines/issues/2886
    // TL;DR: uses immediate dispatcher to circumvent prompt cancellation fallacy (and missed events)
    withContext(Dispatchers.Main.immediate) {
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
}

/**
 * Subscribe to the provider lifecycle-aware.
 * @param lifecycleState the minimum lifecycle state the [LifecycleOwner] must be in to receive updates.
 * @see repeatOnLifecycle
 */
fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.subscribe(
    provider: MVIProvider<S, I, A>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
) where T : LifecycleOwner, T : MVISubscriber<S, A> = subscribe(provider, ::consume, ::render, lifecycleState)

/**
 * Subscribe to the provider lifecycle-aware.
 * @param lifecycleState the minimum lifecycle state the [LifecycleOwner] must be in to receive updates.
 * @see repeatOnLifecycle
 */
fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.subscribe(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
) where T : LifecycleOwner, T : MVIView<S, I, A> = subscribe(provider, lifecycleState)
