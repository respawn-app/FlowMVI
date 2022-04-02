@file:SuppressLint("ComposableNaming")

package com.nek12.flowMVI.android.compose

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.nek12.flowMVI.MVIAction
import com.nek12.flowMVI.MVIIntent
import com.nek12.flowMVI.MVIProvider
import com.nek12.flowMVI.MVIState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.experimental.ExperimentalTypeInference

@Stable
/**
 * An interface for the scope that provides magic [send] and [consume] functions inside your composable
 */
interface MVIIntentScope<in I : MVIIntent, out A : MVIAction> {

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
fun <S : MVIState, I : MVIIntent, A : MVIAction> rememberScope(
    vm: MVIProvider<S, I, A>,
): Lazy<MVIIntentScope<I, A>> = remember(vm) { lazy { MVIIntentScopeImpl(vm) } }

private class MVIIntentScopeImpl<in I : MVIIntent, out A : MVIAction>(
    private val provider: MVIProvider<*, I, A>,
) : MVIIntentScope<I, A> {

    override fun send(intent: I) = provider.send(intent)

    @Composable
    override fun consume(consumer: suspend CoroutineScope.(A) -> Unit) {
        provider.consume(consumer)
    }
}

@Composable
inline fun <A : MVIAction> MVIProvider<*, *, A>.consume(
    crossinline consumer: suspend CoroutineScope.(action: A) -> Unit,
) {
    LaunchedEffect(this) {
        actions.collect {
            launch {
                consumer(it)
            }
        }
    }
}

/**
 * An empty scope for testing and preview purposes. [MVIIntentScope.send] and [MVIIntentScope.consume] do nothing
 */
@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalTypeInference::class)
@Composable
fun <T : MVIIntent, A : MVIAction> EmptyScope(
    @BuilderInference call: @Composable MVIIntentScope<T, A>.() -> Unit,
) = call(EmptyScopeImpl as MVIIntentScope<T, A>)

private object EmptyScopeImpl : MVIIntentScope<MVIIntent, MVIAction> {

    override fun send(intent: MVIIntent) = Unit

    @Composable
    override fun consume(consumer: suspend CoroutineScope.(MVIAction) -> Unit) = Unit
}
