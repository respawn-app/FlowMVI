package com.nek12.flowMVI

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * The state of the view / consumer.
 * The state must be comparable (most likely a data class)
 */
interface MVIState

/**
 * User interaction or other events that happen in the view.
 */
interface MVIIntent

/**
 * A side-effect of processing an [MVIIntent], send by ViewModel / Store.
 */
interface MVIAction

/**
 * Some entity (usually store or a viewmodel) that __provides__ the business logic for the [MVIView].
 */
interface MVIProvider<out S : MVIState, in I : MVIIntent, out A : MVIAction> {

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
     * that require framework dependencies or are reflected in the ui.
     */
    val actions: Flow<A>
}

/**
 * A central business logic unit for handling [MVIIntent]s, [MVIAction]s, and [MVIState]s.
 * A store can function independently of any framework entities or be a part of the view model.
 */
interface MVIStore<S : MVIState, in I : MVIIntent, A : MVIAction> : MVIProvider<S, I, A> {

    /**
     * Set a new state directly, thread-safely and synchronously.
     */
    fun set(state: S)

    /**
     * Send a new UI side-effect to be processed by the **first** view that consumes it, only **once**.
     * Actions not consumed will await in the queue of [Channel.BUFFERED] capacity.
     * Actions that make the capacity overflow will be dropped, starting with the oldest.
     * Actions will be distributed to consumers in a fan-out fashion (although it is not recommended to get yourself
     * into such situations, it is allowed for some corner-cases).
     * @See MVIProvider
     */
    fun send(action: A)
}


/**
 * A [consume]r of [MVIProvider]'s events that has certain state [S].
 * Each view needs a provider, a way to [send] intents to it,
 * a way to [render] the new state, and a way to [consume] side-effects.
 * @see MVIProvider
 */
interface MVIView<S : MVIState, in I : MVIIntent, A : MVIAction> {

    /**
     * Provider, usually a view model, for this view's state and actions.
     */
    val provider: MVIProvider<S, I, A>

    /**
     * Call this when any user interaction or other event occurs that needs business logic processing.
     */
    fun send(intent: I) = provider.send(intent)

    /**
     * Render a new [state]. This function will be called each time [provider] updates the state value.
     * This function should be idempotent and should not [send] any intents.
     */
    fun render(state: S)

    /**
     * Consume a one-time side-effect emitted by [provider]. This function is called each time a side-effect arrives.
     * This function should not [send] intents directly.
     */
    fun consume(action: A)
}
