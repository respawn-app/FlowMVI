package pro.respawn.flowmvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import pro.respawn.flowmvi.action.ActionProvider
import pro.respawn.flowmvi.action.ActionShareBehavior
import pro.respawn.flowmvi.action.SharedStore
import pro.respawn.flowmvi.action.Store
import pro.respawn.flowmvi.base.IntentReceiver
import pro.respawn.flowmvi.base.StateProvider
import pro.respawn.flowmvi.dsl.DelicateStoreApi
import pro.respawn.flowmvi.dsl.FlowMVIDSL
import pro.respawn.flowmvi.internal.ConsumingStore
import pro.respawn.flowmvi.internal.DistributingStore
import pro.respawn.flowmvi.internal.SharedStore
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmName

/**
 * An entity that handles [MVIIntent]s, produces [actions] and manages [states].
 * This is usually the business logic unit.
 */
@Deprecated("Compose your store using Provider, IntentHandler, and ActionHandler interfaces")
public interface MVIProvider<out S : MVIState, in I : MVIIntent, out A : MVIAction> :
    StateProvider<S>,
    IntentReceiver<I>,
    ActionProvider<A>

@Deprecated("Use ActionStore", replaceWith = ReplaceWith("ActionStore<S, I, A>"))
public interface MVIStore<S : MVIState, in I : MVIIntent, A : MVIAction> : MVIProvider<S, I, A>,
    Store<S, I, A>

/**
 * A builder function of [MVIStore]
 */
@Suppress("FunctionName")
@Deprecated("Use store builders")
public fun <S : MVIState, I : MVIIntent, A : MVIAction> MVIStore(
    initial: S,
    /**
     * A behavior to be applied when sharing actions
     * @see ActionShareBehavior
     */
    behavior: ActionShareBehavior = ActionShareBehavior.Distribute(),
    /**
     * State to emit when [reduce] throws.
     *
     *  **Default implementation rethrows the exception**
     *  **The body of this block may be evaluated multiple times in case of concurrent state updates**
     */
    @BuilderInference recover: Recover<S> = { throw it },
    /**
     * Reduce view's intent to a new ui state.
     * Use [MVIStore.send] for sending side-effects for the view to handle.
     * Coroutines launched inside [reduce] can fail independently of each other.
     */
    @BuilderInference reduce: Reduce<S, I, A>,
): Store<S, I, A> = when (behavior) {
    is ActionShareBehavior.Share -> SharedStore(initial, behavior.replay, behavior.buffer, recover, reduce)
    is ActionShareBehavior.Distribute -> DistributingStore(initial, behavior.buffer, recover, reduce)
    is ActionShareBehavior.Restrict -> ConsumingStore(initial, behavior.buffer, recover, reduce)
}

/**
 * A builder function of [MVIStore] that creates  the store lazily. This function does **not** launch the store.
 * Call [MVIStore.start] yourself as appropriate.
 * @see MVIStore
 */
@Deprecated("Use store builders")
public fun <S : MVIState, I : MVIIntent, A : MVIAction> lazyStore(
    initial: S,
    behavior: ActionShareBehavior = ActionShareBehavior.Distribute(),
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    @BuilderInference recover: Recover<S> = { throw it },
    @BuilderInference reduce: Reduce<S, I, A>,
): Lazy<Store<S, I, A>> = lazy(mode) { MVIStore(initial, behavior, recover, reduce) }

/**
 * A builder function of [MVIStore] that creates, and then launches the store lazily on first access.
 * @see MVIStore
 */
@Deprecated("Use store builders")
public fun <S : MVIState, I : MVIIntent, A : MVIAction> launchedStore(
    scope: CoroutineScope,
    initial: S,
    behavior: ActionShareBehavior = ActionShareBehavior.Distribute(),
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    @BuilderInference recover: Recover<S> = { throw it },
    @BuilderInference reduce: Reduce<S, I, A>
): Lazy<Store<S, I, A>> = lazy(mode) { MVIStore(initial, behavior, recover, reduce).apply { start(scope) } }
/**
 * An operation that processes incoming [MVIIntent]s
 */
public typealias Reduce<S, I, A> = suspend ReducerScope<S, I, A>.(intent: I) -> Unit

/**
 * An operation that handles exceptions when processing [MVIIntent]s
 */
public typealias Recover<S> = (e: Exception) -> S

/**
 * A scope of the operation inside [MVIStore].
 * Provides a [CoroutineScope] to use.
 * Throwing when in this scope will result in [Reducer.recover] of the store being called.
 * Child coroutines should handle their exceptions independently, unless using [launchRecovering].
 */
public interface ReducerScope<S : MVIState, in I : MVIIntent, A : MVIAction> {

    /**
     * Delegates to [MVIStore.send]
     */
    public fun send(action: A)

    /**
     * Delegates to [MVIStore.launchRecovering]
     */
    @FlowMVIDSL
    public fun launchRecovering(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        recover: (Recover<S>)? = null,
        block: suspend CoroutineScope.() -> Unit,
    ): Job

    /**
     * Delegates to [MVIStore.withState]
     */
    @FlowMVIDSL
    public suspend fun <R> withState(block: suspend S.() -> R): R

    /**
     * Delegates to [MVIStore.updateState]
     * @see MVIStore.updateState
     * @see [withState]
     */
    @FlowMVIDSL
    public suspend fun updateState(transform: suspend S.() -> S)

    /**
     * Delegates to [MVIStore.state]
     */
    @DelicateStoreApi
    public val state: S

    /**
     * @see [MVIProvider.send]
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("sendAction")
    public fun A.send(): Unit = send(this)
}
