@file:Suppress(
    "ComposableNaming",
    "ComposableEventParameterNaming",
    "TopLevelComposableFunctions",
    "ComposableFunctionName",
    "NOTHING_TO_INLINE"
)

package pro.respawn.flowmvi.android.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.subscribe
import kotlin.experimental.ExperimentalTypeInference

/**
 * An interface for the scope that provides magic [send] and [consume] functions inside your composable
 */
@Stable
public interface ConsumerScope<in I : MVIIntent, out A : MVIAction> : IntentReceiver<I> {
    // composable functions can't have default parameters, so we'll need two overloads

    /**
     * Collect [MVIAction]s that come from the [Store].
     * Should only be called once per screen.
     * Even if you do not have any Actions in your store you still **must** call this function to subscribe to the store
     */
    @Composable
    @FlowMVIDSL
    public fun consume(onAction: suspend CoroutineScope.(action: A) -> Unit)

    /**
     * Collect [MVIState]s emitted by the [Store]
     * Does not subscribe to [MVIAction]s, unlike the other overload
     */
    @Composable
    @FlowMVIDSL
    public fun consume()
}

/**
 * Equivalent to calling [ConsumerScope.consume].
 */
@Composable
@FlowMVIDSL
@NonRestartableComposable
public inline fun ConsumerScope<*, *>.Subscribe(): Unit = consume()

/**
 * Equivalent to calling [ConsumerScope.consume].
 */
@Composable
@FlowMVIDSL
@NonRestartableComposable
public inline fun <A : MVIAction> ConsumerScope<*, A>.Subscribe(
    noinline onAction: suspend CoroutineScope.(action: A) -> Unit
): Unit = consume(onAction)

@Composable
@PublishedApi
internal inline fun <S : MVIState, I : MVIIntent, A : MVIAction> rememberConsumerScope(
    store: Store<S, I, A>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): ConsumerScopeImpl<S, I, A> = remember(store) { ConsumerScopeImpl(store, lifecycleState) }

@Stable
@PublishedApi
internal data class ConsumerScopeImpl<S : MVIState, in I : MVIIntent, out A : MVIAction>(
    private val store: Store<S, I, A>,
    private val lifecycleState: Lifecycle.State = Lifecycle.State.STARTED
) : ConsumerScope<I, A> {

    @PublishedApi
    internal val state: MutableState<S> = mutableStateOf(store.initial)

    override fun send(intent: I) = store.send(intent)
    override suspend fun emit(intent: I) = store.emit(intent)

    @Composable
    private inline fun consumeInternal(noinline onAction: (suspend CoroutineScope.(action: A) -> Unit)?) {
        val owner = LocalLifecycleOwner.current
        val block by rememberUpdatedState(onAction)
        LaunchedEffect(owner, this) {
            owner.repeatOnLifecycle(lifecycleState) {
                subscribe(
                    store = store,
                    consume = block?.let { block -> { block(it) } },
                    render = { state.value = it }
                )
            }
        }
    }

    @Composable
    override fun consume(onAction: suspend CoroutineScope.(action: A) -> Unit) = consumeInternal(onAction)

    @Composable
    override fun consume() = consumeInternal(null)
}

private object EmptyScope : ConsumerScope<MVIIntent, MVIAction> {

    override fun send(intent: MVIIntent) = Unit
    override suspend fun emit(intent: MVIIntent) = Unit

    @Composable
    override fun consume(onAction: suspend CoroutineScope.(action: MVIAction) -> Unit) = Unit

    @Composable
    override fun consume() = Unit
}

/**
 * A no-op scope for testing and preview purposes.
 * [ConsumerScope.send] and [ConsumerScope.consume] do nothing
 */
@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalTypeInference::class)
@Composable
@FlowMVIDSL
public fun <I : MVIIntent, A : MVIAction> EmptyScope(
    @BuilderInference call: @Composable ConsumerScope<I, A>.() -> Unit,
): Unit = call(EmptyScope as ConsumerScope<I, A>)
