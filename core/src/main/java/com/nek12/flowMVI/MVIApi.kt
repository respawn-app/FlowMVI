package com.nek12.flowMVI

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * The state of the view / consumer.
 * The state must be comparable and immutable (most likely a data class)
 */
interface MVIState

/**
 * User interaction or other events that happen in the view.
 * Must be immutable.
 */
interface MVIIntent

/**
 * A side-effect of processing an [MVIIntent], send by ViewModel / Store.
 * Must be immutable.
 */
interface MVIAction

/**
 * Some entity (usually store or a viewmodel) that __provides__ the business logic for the [MVIView].
 */
interface MVIProvider<out S: MVIState, in I: MVIIntent, out A: MVIAction> {

    /**
     * Called when ui-event happens in the view that produces an [intent].
     */
    fun send(intent: I)

    /**
     * A flow of UI states to be handled by the [MVIView].
     */
    val states: StateFlow<S>

    /**
     * A flow of side-effects to be handled by the [MVIView],
     * usually resulting in one-time events happening in the view.
     * actions are distributed to all subscribers equally.
     */
    val actions: Flow<A>
}

/**
 * A central business logic unit for handling [MVIIntent]s, [MVIAction]s, and [MVIState]s.
 * A store can function independently of any framework entities.
 */
interface MVIStore<S: MVIState, in I: MVIIntent, A: MVIAction>: MVIProvider<S, I, A> {

    /**
     * Set a new state directly, thread-safely and synchronously.
     */
    fun set(state: S)

    /**
     * Send a new UI side-effect to be processed by **ALL** subscribers, each only **once**.
     * Actions not consumed will await in the queue with max capacity of 64.
     * Actions that make the capacity overflow will be dropped, starting with the oldest.
     * Actions will be distributed to consumers in an equal fashion, which means each subscriber will receive an action.
     * @See MVIProvider
     */
    fun send(action: A)

    /**
     * Launches store intent processing in a new coroutine on parent thread.
     * Intents are processed as long as parent scope is active
     */
    fun launch(scope: CoroutineScope)
}


/**
 * A [consume]r of [MVIProvider]'s events that has certain state [S].
 * Each view needs a provider, a way to [send] intents to it,
 * a way to [render] the new state, and a way to [consume] side-effects.
 * @see MVIProvider
 */
interface MVIView<S: MVIState, in I: MVIIntent, A: MVIAction> : MVISubscriber<S, A> {

    /**
     * Provider, an object that handles business logic.
     */
    val provider: MVIProvider<S, I, A>

    /**
     * Send an intent for the [provider] to process e.g. a user click.
     */
    fun send(intent: I) = provider.send(intent)

}

interface MVISubscriber<in S: MVIState, in A: MVIAction> {

    /**
     * Render a new [state].
     * This function will be called each time [provider] updates the state value.
     * This function should be idempotent and should not [send] any intents.
     */
    fun render(state: S)

    /**
     * Consume a one-time side-effect emitted by [provider].
     * This function is called each time a side-effect arrives.
     * This function should not [send] intents directly.
     * Each consumer will receive a copy of the [action].
     */
    fun consume(action: A)

}

/**
 * An enum representing how [MVIAction] sharing will be handled in the [MVIStore].
 * There are 3 possible behaviors, which will be different depending on the use-case.
 * When in doubt, use the default one, and change if you have issues only.
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
     * and each action will be distributed to one subscriber at a time.
     * If there are multiple subscribers, only one of them will handle the action,
     * and **the order is unspecified**.
     *
     */
    DISTRIBUTE,

    /**
     * Restricts the count of subscribers to 1. Attempting to subscribe to a store that already has a subscriber
     * will result in an exception.
     */
    RESTRICT
}
