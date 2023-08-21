@file:Suppress(
    "ComposableNaming",
    "ComposableEventParameterNaming",
    "TopLevelComposableFunctions",
    "ComposableFunctionName"
)

package pro.respawn.flowmvi.android.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import pro.respawn.flowmvi.android.subscribe
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import kotlin.experimental.ExperimentalTypeInference

/**
 * An interface for the scope that provides magic [send] and [consume] functions inside your composable
 */
@Stable
public interface ConsumerScope<in I : MVIIntent, out A : MVIAction> : IntentReceiver<I> {

    /**
     * Collect [MVIAction]s that come from the [Store].
     * Should only be called once per screen.
     */
    @Composable
    public fun consume(onAction: suspend CoroutineScope.(action: A) -> Unit)
}

@Composable
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> rememberConsumerScope(
    store: Store<S, I, A>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): ConsumerScopeImpl<S, I, A> = remember(store) { ConsumerScopeImpl(store) }
    .apply { collect(lifecycleState) }

@Stable
internal data class ConsumerScopeImpl<S : MVIState, in I : MVIIntent, A : MVIAction>(
    private val store: Store<S, I, A>,
) : ConsumerScope<I, A> {

    internal val state = mutableStateOf(store.initial)
    private val _actions = MutableSharedFlow<A>()

    override fun send(intent: I) = store.send(intent)
    override suspend fun emit(intent: I) = store.emit(intent)

    @Composable
    override fun consume(onAction: suspend CoroutineScope.(action: A) -> Unit) {
        LaunchedEffect(this) {
            _actions.collect { onAction(it) }
        }
    }

    @Composable
    fun collect(lifecycleState: Lifecycle.State = Lifecycle.State.STARTED) {
        val owner = LocalLifecycleOwner.current
        LaunchedEffect(lifecycleState, this) {
            owner.subscribe(
                lifecycleState = lifecycleState,
                store = store,
                consume = { _actions.emit(it) },
                render = { state.value = it }
            )
        }
    }
}

/**
 * A no-op scope for testing and preview purposes.
 * [ConsumerScope.send] and [ConsumerScope.consume] do nothing
 */
@OptIn(ExperimentalTypeInference::class)
@Composable
@FlowMVIDSL
public fun <I : MVIIntent, A : MVIAction> EmptyScope(
    @BuilderInference call: @Composable ConsumerScope<I, A>.() -> Unit,
): Unit = call(
    object : ConsumerScope<I, A> {
        override fun send(intent: I) = Unit
        override suspend fun emit(intent: I) = Unit

        @Composable
        override fun consume(onAction: suspend CoroutineScope.(action: A) -> Unit) = Unit
    }
)
