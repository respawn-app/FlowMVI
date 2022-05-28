@file:SuppressLint("ComposableNaming")

package com.nek12.flowMVI.android.compose

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import com.nek12.flowMVI.MVIAction
import com.nek12.flowMVI.MVIIntent
import com.nek12.flowMVI.MVIProvider
import com.nek12.flowMVI.MVIState
import kotlinx.coroutines.CoroutineScope

/**
 * An interface for the scope that provides magic [send] and [consume] functions inside your composable
 */
@Stable
interface MVIIntentScope<in I: MVIIntent, out A: MVIAction> {

    /**
     * Send a new intent for the provider you used in [MVIComposable] {
     */
    fun send(intent: I)

    /**
     * Call this somewhere at the top of your [MVIComposable] to consume actions received from the provider
     */
    @Composable
    fun consume(consumer: suspend CoroutineScope.(A) -> Unit)
}

@Composable
internal fun <S: MVIState, I: MVIIntent, A: MVIAction> rememberScope(
    provider: MVIProvider<S, I, A>,
    lifecycleState: Lifecycle.State,
): MVIIntentScope<I, A> = remember(provider, lifecycleState) { MVIIntentScopeImpl(provider, lifecycleState) }

private class MVIIntentScopeImpl<in I: MVIIntent, out A: MVIAction>(
    private val provider: MVIProvider<*, I, A>,
    private val lifecycleState: Lifecycle.State,
): MVIIntentScope<I, A> {

    override fun send(intent: I) = provider.send(intent)

    @Composable
    override fun consume(consumer: suspend CoroutineScope.(A) -> Unit) {
        provider.consume(lifecycleState, consumer)
    }
}

@Composable
fun <A: MVIAction> MVIProvider<*, *, A>.consume(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    consumer: suspend CoroutineScope.(action: A) -> Unit,
) = actions.collectOnLifecycle(lifecycleState, consumer)

/**
 * An empty scope for testing and preview purposes. [MVIIntentScope.send] and [MVIIntentScope.consume] do nothing
 */
@Suppress("UNCHECKED_CAST")
@Composable
fun <T: MVIIntent, A: MVIAction> EmptyScope(
    @BuilderInference call: @Composable MVIIntentScope<T, A>.() -> Unit,
) = call(EmptyScopeImpl as MVIIntentScope<T, A>)

private object EmptyScopeImpl: MVIIntentScope<MVIIntent, MVIAction> {

    override fun send(intent: MVIIntent) = Unit

    @Composable
    override fun consume(consumer: suspend CoroutineScope.(MVIAction) -> Unit) = Unit
}
