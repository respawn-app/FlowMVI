package com.nek12.flowMVI.android.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import com.nek12.flowMVI.MVIAction
import com.nek12.flowMVI.MVIIntent
import com.nek12.flowMVI.MVIProvider
import com.nek12.flowMVI.MVIState

@Composable
/**
 * @param provider an MVIProvider (usually a viewModel) that handles this screen's logic
 * @param content the actual screen content. Will be recomposed each time you receive a new state
 */
inline fun <S : MVIState, I : MVIIntent, A : MVIAction, reified VM : MVIProvider<S, I, A>> MVIComposable(
    provider: VM,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline content: @Composable MVIIntentScope<I, A>.(state: S) -> Unit,
) {

    val scope = rememberScope(provider, lifecycleState)

    val state by provider.states.collectAsStateOnLifecycle(lifecycleState = lifecycleState)

    content(scope, state)
}
