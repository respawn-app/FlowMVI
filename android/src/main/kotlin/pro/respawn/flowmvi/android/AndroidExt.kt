package pro.respawn.flowmvi.android

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIProvider
import pro.respawn.flowmvi.MVIState
import pro.respawn.flowmvi.MVISubscriber
import pro.respawn.flowmvi.MVIView

/**
 *  Subscribe to the [provider] lifecycle-aware.
 *  @param consume called on each new action. Implement action handling here.
 *  @param render called each time the state changes. Render state here.
 *  @param lifecycleState the minimum lifecycle state the [LifecycleOwner] must be in to receive updates.
 *  @see repeatOnLifecycle
 */
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> LifecycleOwner.subscribe(
    provider: MVIProvider<S, I, A>,
    crossinline consume: (action: A) -> Unit,
    crossinline render: (state: S) -> Unit,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): Job = lifecycleScope.launch {
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
 * Subscribe to the store lifecycle-aware.
 * @param lifecycleState the minimum lifecycle state the [LifecycleOwner] must be in to receive updates.
 * @see repeatOnLifecycle
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.subscribe(
    provider: MVIProvider<S, I, A>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): Job where T : LifecycleOwner, T : MVISubscriber<S, A> = subscribe(provider, ::consume, ::render, lifecycleState)

/**
 * Subscribe to the store lifecycle-aware.
 * @param lifecycleState the minimum lifecycle state the [LifecycleOwner] must be in to receive updates.
 * @see repeatOnLifecycle
 */
public fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.subscribe(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): Job where T : LifecycleOwner, T : MVIView<S, I, A> = subscribe(store, lifecycleState)
