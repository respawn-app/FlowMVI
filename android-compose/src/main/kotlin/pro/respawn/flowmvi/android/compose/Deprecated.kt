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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.subscribe
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.experimental.ExperimentalTypeInference

private const val Package = "pro.respawn.flowmvi.android.compose.dsl"

// credits: https://proandroiddev.com/how-to-collect-flows-lifecycle-aware-in-jetpack-compose-babd53582d0b

/**
 * Create and remember a new [Flow] that is only collected when [lifecycleOwner] is in [lifecycleState]
 */
@Deprecated("Use AndroidX collectAsStateWithLifecycle or a LaunchedEffect directly instead")
@Composable
public fun <T> rememberLifecycleFlow(
    flow: Flow<T>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
): Flow<T> = remember(flow, lifecycleOwner, lifecycleState) {
    flow.flowWithLifecycle(lifecycleOwner.lifecycle, lifecycleState)
}

/**
 * Create and collect a [Flow] that is only collected when [LocalLifecycleOwner] is in [lifecycleState]
 */
@Composable
@Deprecated("Use AndroidX collectAsStateWithLifecycle instead")
public fun <T : R, R> Flow<T>.collectAsStateOnLifecycle(
    initial: R,
    context: CoroutineContext = EmptyCoroutineContext,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): State<R> = collectAsStateWithLifecycle(initialValue = initial, minActiveState = lifecycleState, context = context)

/**
 * Create and collect a [Flow] that is only collected when [LocalLifecycleOwner] is in [lifecycleState]
 */
@Suppress("StateFlowValueCalledInComposition")
@Composable
@Deprecated("Use Androidx collectAsStateWithLifecycle instead")
public fun <T> StateFlow<T>.collectAsStateOnLifecycle(
    context: CoroutineContext = EmptyCoroutineContext,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): State<T> = collectAsStateWithLifecycle(minActiveState = lifecycleState, context = context)

/**
 * Create and collect a [Flow] that is only collected when [LocalLifecycleOwner] is in [lifecycleState]
 */
@Composable
@Suppress("ComposableParametersOrdering", "ComposableNaming", "ComposableFunctionName")
@Deprecated("Use Androidx collectAsStateWithLifecycle instead")
public fun <T> Flow<T>.collectOnLifecycle(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend CoroutineScope.(T) -> Unit,
) {
    val lifecycleFlow = rememberLifecycleFlow(this, lifecycleState)
    LaunchedEffect(lifecycleFlow) {
        // see [LifecycleOwner.subscribe] in :android for reasoning
        withContext(Dispatchers.Main.immediate) {
            lifecycleFlow.collect {
                collector(it)
            }
        }
    }
}

/**
 * An interface for the scope that provides [send] and [consume] functions inside your composable
 */
@Stable
@Deprecated("This interface is no longer needed. Use the new subscribe extension instead")
public interface ConsumerScope<in I : MVIIntent, out A : MVIAction> : IntentReceiver<I> {
    // composable functions can't have default parameters, so we'll need two overloads

    /**
     * Collect [MVIAction]s that come from the [Store].
     * Should only be called once per screen.
     * Even if you do not have any Actions in your store you still **must** call this function to subscribe to the store
     */
    @Composable
    @FlowMVIDSL
    @Stable
    public fun consume(onAction: (suspend CoroutineScope.(action: A) -> Unit)?)

    /**
     * Collect [MVIState]s emitted by the [Store]
     * Does not subscribe to [MVIAction]s, unlike the other overload
     */
    @Composable
    @FlowMVIDSL
    @Stable
    public fun consume(): Unit = consume(null)
}

/**
 * Equivalent to calling [ConsumerScope.consume].
 */
@Composable
@FlowMVIDSL
@NonRestartableComposable
@Deprecated("This call is error-prone. Please use the new subscribe dsl with a required parameter")
public inline fun ConsumerScope<*, *>.Subscribe(): Unit = consume()

/**
 * Equivalent to calling [ConsumerScope.consume].
 */
@Composable
@FlowMVIDSL
@NonRestartableComposable
@Deprecated("This call is error-prone. Please use the new subscribe dsl with a required parameter")
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
    override fun consume(onAction: (suspend CoroutineScope.(action: A) -> Unit)?) {
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
}

private object EmptyScope : ConsumerScope<MVIIntent, MVIAction> {

    override fun send(intent: MVIIntent) = Unit
    override suspend fun emit(intent: MVIIntent) = Unit

    @Composable
    override fun consume(onAction: (suspend CoroutineScope.(action: MVIAction) -> Unit)?) = Unit
}

/**
 * A no-op scope for testing and preview purposes.
 * [ConsumerScope.send] and [ConsumerScope.consume] do nothing
 */
@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalTypeInference::class)
@Composable
@FlowMVIDSL
@Deprecated(
    "Please use EmptyReceiver with the new dsl",
    ReplaceWith("EmptyReceiver", Package)
)
public fun <I : MVIIntent, A : MVIAction> EmptyScope(
    @BuilderInference call: @Composable ConsumerScope<I, A>.() -> Unit,
): Unit = call(EmptyScope as ConsumerScope<I, A>)

/**
 * A function that introduces ConsumerScope to the content and ensures safe lifecycle-aware and efficient collection
 * of states and actions.
 *
 * Use [ConsumerScope.consume] to subscribe to the store
 *
 * Usage:
 * ```kotlin
 * @Composable
 * fun HomeScreen() = MVIComposable(getViewModel<HomeViewModel>()) { // this: ConsumerScope<S, I, A>
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
 * @param store a Store (usually a [androidx.lifecycle.ViewModel]) that handles this screen's logic
 * @param lifecycleState the minimum lifecycle state, in which the activity must be to receive actions/states
 * @param content the actual screen content. Will be recomposed each time a new state is received.
 */
@Composable
@Deprecated(
    "This call is error-prone. Please use the new subscribe dsl with a required parameter",
    ReplaceWith(
        "val state by store.subscribe(lifecycleState, consume)",
        Package,
    )
)
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIComposable(
    store: Store<S, I, A>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    @BuilderInference content: @Composable ConsumerScope<I, A>.(state: S) -> Unit,
) {
    val scope = rememberConsumerScope(store, lifecycleState)
    val state by scope.state
    content(scope, state)
}

/**
 * An overload of [MVIComposable] that accepts a [consume] block to automatically
 * subscribe to the [store] upon invocation.
 * @see MVIComposable
 */
@Composable
@Deprecated(
    "This call is error-prone. Please use the new subscribe dsl with a required parameter",
    ReplaceWith(
        "val state by store.subscribe(lifecycleState, consume)",
        Package,
    )
)
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIComposable(
    store: Store<S, I, A>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    noinline consume: (suspend CoroutineScope.(A) -> Unit)?,
    @BuilderInference content: @Composable ConsumerScope<I, A>.(state: S) -> Unit,
) {
    val scope = rememberConsumerScope(store, lifecycleState)
    val state by scope.state
    scope.consume(consume)
    content(scope, state)
}

/**
 * A collection preview param provider that provides [MVIState]
 * Created to avoid boilerplate related to Preview parameters.
 */
@Deprecated(
    "Moved to the \"preview\" package",
    ReplaceWith(
        "StateProvider(states)",
        "pro.respawn.flowmvi.android.compose.preview"
    )
)
public open class StateProvider<S>(
    vararg states: S,
) : CollectionPreviewParameterProvider<S>(states.toList())
