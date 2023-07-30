@file:Suppress("DEPRECATION")

package pro.respawn.flowmvi

import kotlinx.coroutines.CoroutineScope
import pro.respawn.flowmvi.api.ActionConsumer
import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.api.StateConsumer
import pro.respawn.flowmvi.api.StateReceiver
import pro.respawn.flowmvi.api.Store
import kotlin.jvm.JvmName

/**
 * The state of the view / consumer.
 * The state must be comparable and immutable (most likely a data class)
 */
@Deprecated("Moved to api package", ReplaceWith("MVIState", "pro.respawn.flowmvi.api.MVIState"))
public typealias MVIState = pro.respawn.flowmvi.api.MVIState

/**
 * User interaction or other event that happens in the UI layer.
 * Must be immutable.
 */
@Deprecated("Moved to api package", ReplaceWith("MVIIntent", "pro.respawn.flowmvi.api.MVIIntent"))
public typealias MVIIntent = pro.respawn.flowmvi.api.MVIIntent

/**
 * A single, one-shot, side-effect of processing an [MVIIntent], sent by [MVIProvider].
 * Consumed in the ui-layer as a one-time action.
 * Must be immutable.
 */
@Deprecated("Moved to api package", ReplaceWith("MVIAction", "pro.respawn.flowmvi.api.MVIAction"))
public typealias MVIAction = pro.respawn.flowmvi.api.MVIAction

/**
 * An operation that processes incoming [MVIIntent]s
 */
@Deprecated("Moved to plugins package", ReplaceWith("Reduce<S, I, A>", "pro.respawn.flowmvi.plugins.Reduce"))
public typealias Reduce<S, I, A> = suspend ReducerScope<S, I, A>.(intent: I) -> Unit

/**
 * An operation that handles exceptions when processing [MVIIntent]s
 */
@Deprecated(
    "Moved to plugins package with new signature",
    ReplaceWith(
        "pro.respawn.flowmvi.plugins.Recover<S, I, A>",
        "pro.respawn.flowmvi.plugins.Recover",
        "pro.respawn.flowmvi.api.MVIAction",
        "pro.respawn.flowmvi.api.MVIIntent",
        "pro.respawn.flowmvi.api.MVIState",
    )
)
public typealias Recover<S> = (e: Exception) -> S

/**
 * An entity that handles [MVIIntent]s, produces [actions] and manages [states].
 * This is usually the business logic unit.
 */
/**
 * An entity that handles [MVIIntent]s, produces [actions] and manages [states].
 * This is usually the business logic unit.
 */
@Deprecated(
    "renamed to Provider",
    ReplaceWith(
        "Provider<S, I, A>",
        "pro.respawn.flowmvi.api.Provider"
    )
)
public typealias MVIProvider<S, I, A> = Provider<S, I, A>

/**
 * A central business logic unit for handling [MVIIntent]s, [MVIAction]s, and [MVIState]s.
 * Usually not subclassed but used with a corresponding builder (see [lazyStore], [launchedStore]).
 * A store functions independently of any subscribers.
 * MVIStore is the base implementation of [MVIProvider].
 */
@Deprecated("Use Store<S, I, A>", ReplaceWith("Store<S, I, A>", "pro.respawn.flowmvi.api.Store"))
public interface MVIStore<S : MVIState, I : MVIIntent, A : MVIAction> :
    MVIProvider<S, I, A>,
    Store<S, I, A>,
    StateReceiver<S>

/**
 * A [consume]r of [MVIProvider]'s events that has certain state [S].
 * Each [MVIView] needs a provider, a way to [send] intents to it,
 * a way to [render] the new state, and a way to [consume] side-effects.
 * @see MVIProvider
 * @See MVISubscriber
 */
public interface MVIView<S : MVIState, I : MVIIntent, A : MVIAction> : MVISubscriber<S, A> {

    /**
     * Provider, an object that handles business logic.
     * @See MVIProvider
     */
    public val provider: Store<S, I, A>

    /**
     * Send an intent for the [provider] to process e.g. a user click.
     */
    public fun send(intent: I): Unit = provider.send(intent)

    /**
     * @see Store.send
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("sendAction")
    public fun I.send(): Unit = send(this)
}

/**
 * A generic subscriber of [MVIProvider] that [consume]s [MVIAction]s and [render]s [MVIState]s of types [A] and [S].
 * For a more fully defined version, see [MVIView].
 */

@Deprecated(
    "Use StateConsumer, ActionReceiver directly",
    ReplaceWith(
        "StateConsumer<S>, ActionReceiver<A>",
        "pro.respawn.flowmvi.api.StateConsumer",
        "pro.respawn.flowmvi.api.ActionReceiver"
    )
)
public interface MVISubscriber<in S : MVIState, in A : MVIAction> : StateConsumer<S>, ActionConsumer<A>

/**
 * An class representing how [MVIAction] sharing will be handled in the [MVIStore].
 * There are 3 possible behaviors, which will be different depending on the use-case.
 * When in doubt, use the default one, and change if you have issues.
 * @see MVIStore
 */
@Deprecated(
    "Moved to api package",
    ReplaceWith(
        "ActionShareBehavior",
        "pro.respawn.flowmvi.api.ActionShareBehavior"
    )
)
public typealias ActionShareBehavior = pro.respawn.flowmvi.api.ActionShareBehavior

/**
 * An interface for defining a function that will [reduce] incoming [MVIIntent]s.
 * Similar to [Reduce], but can be implemented somewhere else and composed.
 * For ways to convert this to [Reduce] to create a store, see extension functions `recover` and `reduce`.
 */
@Deprecated(
    "Not needed anymore, use Reduce and the new api",
    ReplaceWith("Reduce<S, I, A>", "pro.respawn.flowmvi.plugins.Reduce")
)
public fun interface Reducer<S : MVIState, in I : MVIIntent> {

    /**
     * Reduce consumer's intent to a new [MVIState] or zero or more [MVIAction]s.
     * Use [MVIStore.send] for sending side-effects for the [MVISubscriber] to handle.
     * Coroutines launched inside [reduce] can fail independently of each other.
     */
    // false-positive https://youtrack.jetbrains.com/issue/KTIJ-7642
    @Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION")
    public suspend fun CoroutineScope.reduce(intent: I)

    /**
     * State to emit when [reduce] throws.
     *
     *  **Default implementation rethrows the exception.**
     *  **The body of this block may be evaluated multiple times in case of concurrent state updates**
     */
    public fun recover(from: Exception): S = throw from
}

/**
 * A scope of the operation inside [MVIStore].
 * Provides a [CoroutineScope] to use.
 * Throwing when in this scope will result in [Reducer.recover] of the store being called.
 */
public typealias ReducerScope<S, I, A> = PipelineContext<S, I, A>

/**
 * A mutable version of the [Store] that implements [StateReceiver] and [ActionReceiver].
 */
@Deprecated(
    """
This will be removed in the future, because all store operations can happen inside the store now.
This is only used to support MVIViewModel and other Deprecated APIs.
"""
)
public interface MutableStore<S : MVIState, I : MVIIntent, A : MVIAction> :
    Store<S, I, A>,
    StateReceiver<S>,
    ActionReceiver<A>
