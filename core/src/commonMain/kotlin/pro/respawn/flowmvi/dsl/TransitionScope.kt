@file:Suppress("UNCHECKED_CAST")

package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.annotation.NotIntendedForInheritance
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext

/**
 * Scope available inside FSM [on][StateTransitionsBuilder.on] handlers.
 * Delegates to [PipelineContext] for full store API access, and adds
 * typed [state] and convenience [transitionTo] methods.
 *
 * Everything you can do in [reduce][pro.respawn.flowmvi.plugins.reduce], you can do here — plus you get the current
 * state guaranteed to be of type [T], and a shorthand for state transitions.
 */
@FlowMVIDSL
@OptIn(ExperimentalSubclassOptIn::class)
@SubclassOptInRequired(NotIntendedForInheritance::class)
public interface TransitionScope<out T : S, S : MVIState, I : MVIIntent, A : MVIAction> :
    PipelineContext<S, I, A> {

    /**
     * The current state, guaranteed to be of type [T] at the time this handler was invoked.
     *
     * **Note**: If [pro.respawn.flowmvi.api.StoreConfiguration.parallelIntents] is `true`, the actual store state
     * may have changed by the time you read this. Use [updateState] for atomic operations.
     * For most use cases this value is stable because the default is sequential intent processing.
     */
    public val state: T

    /**
     * Transition to [target] state. Shorthand for `updateState { target }`.
     * The transition will be validated by the FSM's enforcement rules via `onState`.
     */
    @FlowMVIDSL
    public suspend fun transitionTo(target: S)

    /**
     * Transition to [target] state and emit [sideEffect] action.
     * Shorthand for `updateState { target }; action(sideEffect)`.
     */
    @FlowMVIDSL
    public suspend fun transitionTo(target: S, sideEffect: A)
}

@OptIn(NotIntendedForInheritance::class)
@PublishedApi
internal class TransitionScopeImpl<T : S, S : MVIState, I : MVIIntent, A : MVIAction>(
    private val pipeline: PipelineContext<S, I, A>,
    override val state: T,
) : TransitionScope<T, S, I, A>, PipelineContext<S, I, A> by pipeline {

    override suspend fun transitionTo(target: S) {
        updateState { target }
    }

    override suspend fun transitionTo(target: S, sideEffect: A) {
        updateState { target }
        action(sideEffect)
    }
}
