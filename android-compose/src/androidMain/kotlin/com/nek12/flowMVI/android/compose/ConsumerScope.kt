@file:Suppress(
    "ComposableNaming",
    "ComposableEventParameterNaming",
    "TopLevelComposableFunctions",
    "ComposableFunctionName"
)

package com.nek12.flowMVI.android.compose

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
interface ConsumerScope<in I : MVIIntent, out A : MVIAction> {

    /**
     * Send a new intent for the provider you used in [MVIComposable] {
     */
    fun send(intent: I)

    /**
     * Call this somewhere at the top of your [MVIComposable] to consume actions received from the provider
     */
    @Composable
    fun consume(consumer: suspend CoroutineScope.(A) -> Unit)

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("sendAction")
    fun I.send() = send(this)
}

@Composable
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> rememberConsumerScope(
    provider: MVIProvider<S, I, A>,
    lifecycleState: Lifecycle.State,
): ConsumerScope<I, A> = remember(provider, lifecycleState) { ConsumerScopeImpl(provider, lifecycleState) }

private class ConsumerScopeImpl<in I : MVIIntent, out A : MVIAction>(
    private val provider: MVIProvider<*, I, A>,
    private val lifecycleState: Lifecycle.State,
) : ConsumerScope<I, A> {

    override fun send(intent: I) = provider.send(intent)

    @Composable
    override fun consume(consumer: suspend CoroutineScope.(A) -> Unit) {
        provider.consume(lifecycleState, consumer)
    }
}

@Composable
@Suppress("ComposableParametersOrdering")
fun <A : MVIAction> MVIProvider<*, *, A>.consume(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    onAction: suspend CoroutineScope.(action: A) -> Unit,
) = actions.collectOnLifecycle(lifecycleState, onAction)

/**
 * A no-op scope for testing and preview purposes.
 * [ConsumerScope.send] and [ConsumerScope.consume] do nothing
 */
@Suppress("UNCHECKED_CAST")
@Composable
fun <T : MVIIntent, A : MVIAction> EmptyScope(
    @BuilderInference call: @Composable ConsumerScope<T, A>.() -> Unit,
) = call(EmptyScopeImpl as ConsumerScope<T, A>)

private object EmptyScopeImpl : ConsumerScope<MVIIntent, MVIAction> {

    override fun send(intent: MVIIntent) = Unit

    @Composable
    override fun consume(consumer: suspend CoroutineScope.(MVIAction) -> Unit) = Unit
}
