package com.nek12.flowMVI.android.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import com.nek12.flowMVI.MVIAction
import com.nek12.flowMVI.MVIIntent
import com.nek12.flowMVI.MVIProvider
import com.nek12.flowMVI.MVIState
import kotlinx.coroutines.Dispatchers

/**
 * A function that introduces MVIIntentScope to the content and ensures safe lifecycle-aware and efficient collection
 * of states and actions.
 * Usage:
 * ```
 * @Composable
 * fun HomeScreen() = MVIComposable(getViewModel<HomeViewModel>()) { state ->
 *     consume { action ->
 *         when(action) {
 *             /*...*/
 *         }
 *     }
 *     when(state) {
 *         //use state to render content
 *     }
 * }
 * ```
 * @param provider an MVIProvider (usually a viewModel) that handles this screen's logic
 * @param lifecycleState the minimum lifecycle state, in which the activity must be to receive actions/states
 * @param content the actual screen content. Will be recomposed each time a new state is received.
 */
@Composable
fun <S : MVIState, I : MVIIntent, A : MVIAction, VM : MVIProvider<S, I, A>> MVIComposable(
    provider: VM,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    content: @Composable MVIIntentScope<I, A>.(state: S) -> Unit,
) {

    val scope = rememberScope(provider, lifecycleState)

    // see [LifecycleOwner.subscribe] in :android for reasoning behind the dispatcher
    val state by provider.states.collectAsStateOnLifecycle(Dispatchers.Main.immediate, lifecycleState)

    content(scope, state)
}
