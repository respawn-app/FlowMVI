package pro.respawn.flowmvi.plugins

import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.annotation.InternalFlowMVIAPI
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.dsl.collect
import kotlin.reflect.KClass

/**
 * Internal handler function. Takes PipelineContext + current state + intent.
 * The PipelineContext is the receiver so handlers can call updateState/action/intent/launch.
 */
internal typealias IntentHandler<S, I, A> = suspend PipelineContext<S, I, A>.(state: S, intent: I) -> Unit

/**
 * Immutable transition graph that maps (StateType, IntentType) → handler.
 */
@PublishedApi
internal data class TransitionGraph<S : MVIState, I : MVIIntent, A : MVIAction>(
    /** Map from state KClass to its [StateDefinition]. */
    val definitions: Map<KClass<out S>, StateDefinition<S, I, A>>,
)

/**
 * All intent handlers registered for a particular state type.
 */
@PublishedApi
internal data class StateDefinition<S : MVIState, I : MVIIntent, A : MVIAction>(
    /** The KClass of the state this definition applies to. */
    val stateType: KClass<out S>,
    /** Map from intent KClass to handler. */
    val handlers: Map<KClass<out I>, IntentHandler<S, I, A>>,
    /** Child store compositions scoped to this state type. */
    val compositions: List<ComposeDefinition<S, I, A, *, *, *>> = emptyList(),
)

/**
 * Internal representation of a `compose()` call in the transitions DSL.
 * Captures the child store, merge function, optional action consumer, and scope constraint.
 */
@PublishedApi
@Suppress("UseDataClass") // holds function references; equality is not meaningful
internal class ComposeDefinition<
    S : MVIState,
    I : MVIIntent,
    A : MVIAction,
    CS : MVIState,
    CI : MVIIntent,
    CA : MVIAction,
    >(
    /** The child store to subscribe to. */
    val store: Store<CS, CI, CA>,
    /** Merges child state into parent state. */
    val merge: S.(CS) -> S,
    /** Optional handler for child actions, running in parent's PipelineContext. */
    val consume: (suspend PipelineContext<S, I, A>.(CA) -> Unit)?,
    /** If non-null, composition is active only while parent is in this state type.
     *  If null, always active (top-level). */
    val scopedToState: KClass<out S>?,
) {

    @OptIn(InternalFlowMVIAPI::class)
    @Suppress("UNCHECKED_CAST")
    internal fun launchIn(ctx: PipelineContext<S, I, A>): Job = with(ctx) {
        launch {
            store.collect {
                launch {
                    states.collect { childState ->
                        updateState { (merge as S.(Any?) -> S)(childState) }
                    }
                }
                consume?.let { consumeFn ->
                    launch {
                        actions.collect { childAction ->
                            (consumeFn as suspend PipelineContext<S, I, A>.(Any?) -> Unit)
                                .invoke(ctx, childAction)
                        }
                    }
                }
                awaitCancellation()
            }
        }
    }
}
