package pro.respawn.flowmvi.api.context

import pro.respawn.flowmvi.annotation.NotIntendedForInheritance
import pro.respawn.flowmvi.api.ImmediateStateReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StoreConfiguration
import pro.respawn.flowmvi.api.lifecycle.StoreLifecycle

/**
 * The context in which the store may be shut down.
 *
 * Provides a limited API to manipulate values that outlive the full [PipelineContext].
 */
@OptIn(ExperimentalSubclassOptIn::class)
@SubclassOptInRequired(NotIntendedForInheritance::class)
public interface ShutdownContext<S : MVIState, I : MVIIntent, A : MVIAction> :
    ImmediateStateReceiver<S>,
    StoreLifecycle {

    /**
     * The [StoreConfiguration] of this store
     */
    public val config: StoreConfiguration<S>
}
