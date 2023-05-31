@file:Suppress(
    "ComposableNaming",
    "ComposableEventParameterNaming",
    "TopLevelComposableFunctions",
    "ComposableFunctionName"
)

package pro.respawn.flowmvi.android.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.dsl.FlowMVIDSL
import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIProvider
import pro.respawn.flowmvi.MVIState
import kotlin.experimental.ExperimentalTypeInference

/**
 * An interface for the scope that provides magic [send] and [consume] functions inside your composable
 */
@Stable
@FlowMVIDSL
public interface ConsumerScope<in I : MVIIntent, out A : MVIAction> {

    /**
     * Send a new intent for the store you used in [MVIComposable]
     * @see MVIProvider.send
     */
    public fun send(intent: I)

    /**
     * Call this somewhere at the top of your [MVIComposable] to consume actions received from the store
     * @see MVIProvider.consume
     */
    @Composable
    public fun consume(consumer: suspend CoroutineScope.(A) -> Unit)

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("sendAction")
    /**
     * @see send
     */
    public fun I.send(): Unit = send(this)
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
@FlowMVIDSL
@Suppress("ComposableParametersOrdering")
/**
 * @see [ConsumerScope.consume]
 */
public fun <A : MVIAction> MVIProvider<*, *, A>.consume(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    onAction: suspend CoroutineScope.(action: A) -> Unit,
): Unit = actions.collectOnLifecycle(lifecycleState, onAction)

/**
 * A no-op scope for testing and preview purposes.
 * [ConsumerScope.send] and [ConsumerScope.consume] do nothing
 */
@OptIn(ExperimentalTypeInference::class)
@Suppress("UNCHECKED_CAST")
@Composable
@FlowMVIDSL
public fun <T : MVIIntent, A : MVIAction> EmptyScope(
    @BuilderInference call: @Composable ConsumerScope<T, A>.() -> Unit,
): Unit = call(EmptyScopeImpl as ConsumerScope<T, A>)

private object EmptyScopeImpl : ConsumerScope<MVIIntent, MVIAction> {

    override fun send(intent: MVIIntent) = Unit

    @Composable
    override fun consume(consumer: suspend CoroutineScope.(MVIAction) -> Unit) = Unit
}
