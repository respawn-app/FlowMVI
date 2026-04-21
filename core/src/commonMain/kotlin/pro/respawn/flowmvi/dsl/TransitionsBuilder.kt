@file:Suppress("UNCHECKED_CAST")

package pro.respawn.flowmvi.dsl

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.plugins.ComposeDefinition
import pro.respawn.flowmvi.plugins.StateDefinition
import pro.respawn.flowmvi.plugins.TransitionGraph
import kotlin.reflect.KClass

/**
 * Builder that collects state definitions and compiles a [TransitionGraph].
 */
@FlowMVIDSL
public class TransitionsBuilder<S : MVIState, I : MVIIntent, A : MVIAction>
@PublishedApi internal constructor() {

    @PublishedApi
    internal val definitions: MutableMap<KClass<out S>, StateDefinition<S, I, A>> = mutableMapOf()

    @PublishedApi
    internal val topLevelCompositions: MutableList<ComposeDefinition<S, I, A, *, *, *>> = mutableListOf()

    /**
     * Compose a child store into this store's state. The child's state changes are merged into the
     * parent state at all times while the parent store is active.
     *
     * The child store is automatically started as a lifecycle child of the parent store.
     *
     * @param store The child store to compose
     * @param merge Maps the child's state into the parent's state. Called each time the child state changes.
     *   Receiver is the current parent state; parameter is the new child state. Returns the new parent state.
     * @param consume Optional lambda to handle actions emitted by the child store. Runs in the parent's
     *   [PipelineContext], so you can call [action][pro.respawn.flowmvi.api.ActionReceiver.action],
     *   [intent][pro.respawn.flowmvi.api.IntentReceiver.intent], `updateState`, etc.
     */
    @FlowMVIDSL
    public fun <CS : MVIState, CI : MVIIntent, CA : MVIAction> compose(
        store: Store<CS, CI, CA>,
        merge: S.(childState: CS) -> S,
        consume: (suspend PipelineContext<S, I, A>.(CA) -> Unit)? = null,
    ) {
        topLevelCompositions += ComposeDefinition(
            store = store,
            merge = merge,
            consume = consume,
            scopedToState = null,
        )
    }

    /**
     * Define intent handlers for state type [T].
     * Only intents registered via [StateTransitionsBuilder.on] will be handled when the store is in state [T].
     *
     * @throws IllegalArgumentException if [T] is already defined in this transitions block.
     */
    @FlowMVIDSL
    public inline fun <reified T : S> state(
        @BuilderInference block: StateTransitionsBuilder<T, S, I, A>.() -> Unit,
    ) {
        val stateClass = T::class
        require(stateClass !in definitions) {
            "State ${stateClass.simpleName} is already defined in this transitions block"
        }
        val builder = StateTransitionsBuilder<T, S, I, A>(stateClass).apply(block)
        definitions[stateClass] = builder.build()
    }

    @PublishedApi
    internal fun build(): TransitionGraph<S, I, A> = TransitionGraph(
        definitions = definitions.toMap(),
    )
}

/**
 * Builder for declaring intent handlers within a specific state type [T].
 */
@FlowMVIDSL
public class StateTransitionsBuilder<T : S, S : MVIState, I : MVIIntent, A : MVIAction>
@PublishedApi internal constructor(
    @PublishedApi internal val stateType: KClass<out S>,
) {

    @PublishedApi
    internal val handlers: MutableMap<KClass<out I>, suspend PipelineContext<S, I, A>.(state: S, intent: I) -> Unit> =
        mutableMapOf()

    @PublishedApi
    internal val compositions: MutableList<ComposeDefinition<S, I, A, *, *, *>> = mutableListOf()

    /**
     * Compose a child store into this store's state, scoped to state type [T].
     *
     * The child subscription is **only active while the parent store is in state [T]**.
     * When the parent transitions into [T], the subscription starts and the child's current state
     * is immediately merged. When the parent transitions out of [T], the subscription is cancelled.
     *
     * The child store is automatically started as a lifecycle child of the parent store
     * (it outlives individual state scopes and is stopped when the parent store stops).
     *
     * @param store The child store to compose
     * @param merge Maps the child's state into the parent's state. Receiver is the current parent state
     *   typed as [T]; returns the new parent state (may be any [S] to support transitions).
     * @param consume Optional lambda to handle actions emitted by the child store.
     */
    @FlowMVIDSL
    public fun <CS : MVIState, CI : MVIIntent, CA : MVIAction> compose(
        store: Store<CS, CI, CA>,
        merge: T.(childState: CS) -> S,
        consume: (suspend PipelineContext<S, I, A>.(CA) -> Unit)? = null,
    ) {
        compositions += ComposeDefinition(
            store = store,
            merge = merge as S.(Any) -> S, // widened for storage; safety ensured by scoped activation
            consume = consume,
            scopedToState = stateType,
        )
    }

    /**
     * Handle intent of type [E] when the store is in state [T].
     * Inside the lambda, you have full [pro.respawn.flowmvi.api.PipelineContext] access
     * plus typed [TransitionScope.state].
     * Use [TransitionScope.transitionTo] to change state,
     * or call `updateState`/`action`/`intent`/`launch` directly.
     *
     * @throws IllegalArgumentException if [E] is already handled for state [T].
     */
    @FlowMVIDSL
    public inline fun <reified E : I> on(
        noinline block: suspend TransitionScope<T, S, I, A>.(E) -> Unit,
    ) {
        val intentClass = E::class
        require(intentClass !in handlers) {
            "Intent ${intentClass.simpleName} is already handled for state ${stateType.simpleName}"
        }
        handlers[intentClass] = handler@{ state, intent ->
            val scope = TransitionScopeImpl<T, S, I, A>(this, state as T)
            scope.block(intent as E)
        }
    }

    @PublishedApi
    internal fun build(): StateDefinition<S, I, A> = StateDefinition(
        stateType = stateType,
        handlers = handlers.toMap(),
        compositions = compositions.toList(),
    )
}
