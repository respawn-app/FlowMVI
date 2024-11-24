package pro.respawn.flowmvi.api.context

import pro.respawn.flowmvi.annotation.NotIntendedForInheritance
import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.ImmediateStateReceiver
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StoreConfiguration

@OptIn(ExperimentalSubclassOptIn::class)
@SubclassOptInRequired(NotIntendedForInheritance::class)
public interface UndeliveredHandlerContext<S : MVIState, I : MVIIntent, A : MVIAction> :
    ImmediateStateReceiver<S>,
    IntentReceiver<I>,
    ActionReceiver<A> {

    /**
     * The [StoreConfiguration] of this store
     */
    public val config: StoreConfiguration<S>
}
