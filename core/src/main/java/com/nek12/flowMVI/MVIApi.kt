package com.nek12.flowMVI

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * The state of the view / consumer.
 * The state must be comparable and immutable (most likely a data class)
 */
interface MVIState

/**
 * User interaction or other event that happens in the UI layer.
 * Must be immutable.
 */
interface MVIIntent

/**
 * A single, one-shot, side-effect of processing an [MVIIntent], sent by [MVIProvider].
 * Consumed in the ui-layer as a one-time action.
 * Must be immutable.
 */
interface MVIAction

/**
 * An operation that processes incoming [MVIIntent]s
 */
typealias Reducer<S, I, A> = suspend ReducerScope<S, I, A>.(intent: I) -> Unit

/**
 * An operation that handles exceptions when processing [MVIIntent]s
 */
typealias Recover<S> = (e: Exception) -> S

/**
 * An entity that handles [MVIIntent]s sent by UI layer and manages UI [states].
 * This is usually the business logic unit
 */
interface MVIProvider<out S : MVIState, in I : MVIIntent, out A : MVIAction> {

    /**
     * Should be called when a UI event happens that produces an [intent].
     * @See MVIIntent
     */
    fun send(intent: I)

    /**
     * A flow of UI states to be handled by the [MVISubscriber].
     */
    val states: StateFlow<S>

    /**
     * A flow of [MVIAction]s to be handled by the [MVISubscriber],
     * usually resulting in one-shot events.
     * How actions are distributed depends on [ActionShareBehavior].
     */
    val actions: Flow<A>

    // the library does not support java, and Kotlin does not allow
    // overridable @JvmName because of java interop so its' safe to suppress this
    // will be solved by context receivers
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("sendIntent")
    fun I.send() = send(this)
}

/**
 * A central business logic unit for handling [MVIIntent]s, [MVIAction]s, and [MVIState]s.
 * A store functions independently of any subscribers.
 * MVIStore is the base implementation of [MVIProvider].
 */
interface MVIStore<S : MVIState, in I : MVIIntent, A : MVIAction> : MVIProvider<S, I, A> {

    /**
     * Send a new UI side-effect to be processed by subscribers, only once.
     * Actions not consumed will await in the queue with max capacity of 64 by default.
     * Actions that make the capacity overflow will be dropped, starting with the oldest.
     * How actions will be distributed depends on [ActionShareBehavior].
     * @See MVIProvider
     */
    fun send(action: A)

    /**
     * Starts store intent processing in a new coroutine on the parent thread.
     * Intents are processed as long as the parent scope is active.
     * **Starting store processing when it is already started will result in an exception**
     * Although not advised, store can experimentally be launched multiple times, provided you cancel the job used before.
     * @return a [Job] that the store is running on that can be cancelled later.
     */
    fun start(scope: CoroutineScope): Job

    /**
     * Obtain or set the current state in an unsafe manner.
     * This property is not thread-safe and parallel state updates will introduce a race condition when not
     * handled properly.
     */
    @DelicateStoreApi
    val state: S

    /**
     * Obtain the current [MVIStore.state] and update it with the result of [transform].
     *
     * **This function will suspend until all previous [MVIStore.withState] invocations are finished.**
     * **[transform] may be evaluated multiple times if the state is being assigned concurrently.**
     * **This function is not reentrant, for more info, see [MVIStore.withState].**
     *
     * If you want to operate on a state of particular subtype, use the typed version of this function.
     * @see [withState]
     */
    suspend fun updateState(transform: suspend S.() -> S): S

    /**
     * Obtain the current state and operate on it, returning [R].
     *
     * This function does NOT update the state, for that, use [updateState].
     * Store allows only one state update at a time, and because of that,
     *
     * **every coroutine that will invoke [withState] or [updateState]
     * will be suspended until the previous update is finished.**
     *
     * This function uses locks under the hood.
     * For a version that runs when the state is of particular subtype, see other overloads of this function.
     *
     * This function is **not** reentrant, which means, if you call:
     * ```kotlin
     * withState {
     *   withState { }
     * }
     * ```
     * **you will get a deadlock**
     *
     * @returns the value of [R], i.e. the result of the block.
     */
    suspend fun <R> withState(block: suspend S.() -> R): R

    /**
     * Launch a new coroutine using given [scope],
     * and use either provided [recover] block or the [MVIStore]'s recover block.
     * Exceptions thrown in the [block] or in the nested coroutines will be handled by [recover].
     * This function does not update or obtain the state, for that, use [withState] or [updateState] inside [block].
     */
    fun launchRecovering(
        scope: CoroutineScope,
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        recover: Recover<S>? = null,
        block: suspend CoroutineScope.() -> Unit,
    ): Job
}

/**
 * A [consume]r of [MVIProvider]'s events that has certain state [S].
 * Each view needs a provider, a way to [send] intents to it,
 * a way to [render] the new state, and a way to [consume] side-effects.
 * @see MVIProvider
 * @See MVISubscriber
 */
interface MVIView<S : MVIState, in I : MVIIntent, A : MVIAction> : MVISubscriber<S, A> {

    /**
     * Provider, an object that handles business logic.
     * @See MVIProvider
     */
    val provider: MVIProvider<S, I, A>

    /**
     * Send an intent for the [provider] to process e.g. a user click.
     */
    fun send(intent: I) = provider.send(intent)

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("sendAction")
    fun I.send() = send(this)
}

/**
 * A generic subscriber of [MVIProvider] that [consume]s [MVIAction]s and [render]s [MVIState]s of types [A] and [S].
 */
interface MVISubscriber<in S : MVIState, in A : MVIAction> {

    /**
     * Render a new [state].
     * This function will be called each time a new state is received.
     *
     * This function should be idempotent and should not send any intents.
     */
    fun render(state: S)

    /**
     * Consume a one-time side-effect emitted by [MVIProvider].
     *
     * This function is called each time an [MVIAction] arrives.
     * This function may send intents under the promise that no loops will occur.
     */
    fun consume(action: A)
}

/**
 * An enum representing how [MVIAction] sharing will be handled in the [MVIStore].
 * There are 3 possible behaviors, which will be different depending on the use-case.
 * When in doubt, use the default one, and change if you have issues.
 * @see MVIStore
 */
enum class ActionShareBehavior {

    /**
     * Actions will be distributed to all subscribers equally. Each subscriber will receive a reference to a single
     * instance of the action that was sent from any provider. Use when you want to have multiple subscribers
     * that each consume actions. Be aware that, however, if there's at least one subscriber, they will consume an
     * action entirely (i.e. other subscribers won't receive it when they "return" if they weren't present at the
     * time of emission).
     */
    SHARE,

    /**
     * Fan-out behavior means that multiple subscribers are allowed,
     * and each action will be distributed to one subscriber.
     * If there are multiple subscribers, only one of them will handle an instance of an action,
     * and **the order is unspecified**.
     *
     * **This is the default**.
     */
    DISTRIBUTE,

    /**
     * Restricts the count of subscribers to 1.
     * Attempting to subscribe to a store that has already been subscribed to will result in an exception.
     * In other words, you will be required to create a new store for each caller of [subscribe].
     *
     * **Resubscriptions are not allowed too, including lifecycle-aware collection**.
     */
    RESTRICT
}

/**
 * A scope of the operation inside [MVIStore].
 * Provides a [CoroutineScope] to use.
 * Throwing when in this scope will result in recover() of the parent store being called.
 * Child coroutines should handle their exceptions independently, unless using [launchRecovering].
 */
interface ReducerScope<S : MVIState, in I : MVIIntent, A : MVIAction> {

    /**
     * A coroutine scope the intent processing runs on. This is a child scope that is used when
     * [MVIStore.start] is called.
     *
     * **Cancelling the scope will cancel the [MVIStore.start] (intent processing)**.
     */
    val scope: CoroutineScope

    /**
     * Delegates to [MVIStore.send]
     */
    fun send(action: A)

    /**
     * Delegates to [MVIStore.launchRecovering]
     */
    fun launchRecovering(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        recover: (Recover<S>)? = null,
        block: suspend CoroutineScope.() -> Unit,
    ): Job

    /**
     * Delegates to [MVIStore.withState]
     */
    suspend fun <R> withState(block: suspend S.() -> R): R

    /**
     * Delegates to [MVIStore.updateState]
     * @see MVIStore.updateState
     * @see [withState]
     */
    suspend fun updateState(transform: suspend S.() -> S): S

    /**
     * Delegates to [MVIStore.state]
     */
    @DelicateStoreApi
    val state: S

    // the library does not support java, and Kotlin does not allow
    // overridable @JvmName because of java interop so its' safe to suppress this
    // will be solved by context receivers
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("sendAction")
    fun A.send() = send(this)
}
