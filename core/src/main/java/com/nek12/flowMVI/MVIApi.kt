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

typealias Reducer<S, I, A> = suspend MVIStoreScope<S, I, A>.(I) -> Unit
typealias Recover<S> = (e: Exception) -> S

/**
 * An entity that handles [MVIIntent]s sent by UI layer and manages UI [states]
 */
interface MVIProvider<out S : MVIState, in I : MVIIntent, out A : MVIAction> {

    /**
     * Should be called when a UI event happens that produces an [intent].
     * @See MVIIntent
     */
    fun send(intent: I)

    /**
     * A flow of UI states to be handled by the [MVIView].
     */
    val states: StateFlow<S>

    /**
     * A flow of [MVIAction]s to be handled by the [MVIView],
     * usually resulting in one-shot events.
     * How actions are distributed depends on [ActionShareBehavior].
     */
    val actions: Flow<A>
}

/**
 * A central business logic unit for handling [MVIIntent]s, [MVIAction]s, and [MVIState]s.
 * A store functions independently of any subscribers.
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
     * Launches store intent processing in a new coroutine on parent thread.
     * Intents are processed as long as parent scope is active.
     * launching store collection when it is already launched will result in an exception.
     * Although not advised, store can experimentally be launched multiple times.
     */
    fun start(scope: CoroutineScope): Job

    @DelicateStoreApi
    var state: S

    suspend fun <R> withState(block: suspend S.() -> R): R

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
}

interface MVISubscriber<in S : MVIState, in A : MVIAction> {

    /**
     * Render a new [state].
     * This function will be called each time a new state is received
     * This function should be idempotent and should not send any intents.
     */
    fun render(state: S)

    /**
     * Consume a one-time side-effect emitted by [MVIProvider].
     * This function is called each time an [MVIAction] arrives.
     * This function should not send intents.
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
     */
    DISTRIBUTE,

    /**
     * Restricts the count of subscribers to 1.
     * Attempting to subscribe to a store that has already been subscribed to will result in an exception.
     * In other words, you will be required to create a new store for each caller of [subscribe].
     * **This is the default**.
     */
    RESTRICT
}

/**
 * A scope of the operation inside [MVIStore].
 * Provides a [CoroutineScope] to use.
 * **Cancelling the scope will cancel the store.launch() (intent processing)**.
 * Throwing when in this scope will result in recover() of the parent store being called.
 * Child coroutines should handle their exceptions independently, unless using [launchForState].
 */
interface MVIStoreScope<S : MVIState, in I : MVIIntent, A : MVIAction> {

    /**
     * @see MVIStore.send
     */
    fun send(action: A)

    fun launchRecovering(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        recover: (Recover<S>)? = null,
        block: suspend CoroutineScope.() -> Unit,
    ): Job

    suspend fun <R> withState(block: suspend S.() -> R): R

    @DelicateStoreApi
    var state: S
}
