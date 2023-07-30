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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emptyFlow
import pro.respawn.flowmvi.android.subscribe
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import kotlin.experimental.ExperimentalTypeInference

/**
 * An interface for the scope that provides magic [send] and [consume] functions inside your composable
 */
@Stable
public interface ConsumerScope<in I : MVIIntent, out A : MVIAction> {

    /**
     * Send a new intent for the store you used in [MVIComposable]
     * @see pro.respawn.flowmvi.api.IntentReceiver.send
     */
    public fun send(intent: I)

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("sendAction")
    /**
     * @see send
     */
    public fun I.send(): Unit = send(this)

    /**
     * A property that is being delegated to when calling [Store.subscribe] in composition
     */
    public val actions: Flow<A>
}

@Composable
internal fun <S : MVIState, I : MVIIntent, A : MVIAction> rememberConsumerScope(
    store: Store<S, I, A>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): ConsumerScopeImpl<S, I, A> {
    val scope = remember(store) { ConsumerScopeImpl(store) }
    scope.collect(lifecycleState)
    return scope
}

@Stable
internal class ConsumerScopeImpl<S : MVIState, in I : MVIIntent, A : MVIAction>(
    private val store: Store<S, I, A>,
) : ConsumerScope<I, A> {

    internal val state = mutableStateOf(store.initial)
    private val _actions = Channel<A>(Channel.UNLIMITED)

    override fun send(intent: I) = store.send(intent)
    override val actions = _actions.consumeAsFlow()
    override fun hashCode(): Int = store.hashCode()
    override fun equals(other: Any?) = store == other

    @Composable
    fun collect(lifecycleState: Lifecycle.State = Lifecycle.State.STARTED) {
        val owner = LocalLifecycleOwner.current
        LaunchedEffect(owner, lifecycleState) {
            owner.subscribe(
                lifecycleState = lifecycleState,
                store = store,
                consume = { _actions.send(it) },
                render = { state.value = it }
            )
        }
    }
}

/**
 * Basically an alias for [ConsumerScope.actions].collect()
 */
@Composable
@FlowMVIDSL
public fun <A : MVIAction> ConsumerScope<*, A>.consume(
    onAction: suspend CoroutineScope.(action: A) -> Unit,
): Unit = LaunchedEffect(Unit) {
    actions.collect { onAction(it) }
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
        override val actions: Flow<A> = emptyFlow()
    }
)
