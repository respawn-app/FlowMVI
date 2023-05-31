package pro.respawn.flowmvi.action

import pro.respawn.flowmvi.MVIAction
import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState

public class StoreConfiguration<S : MVIState, I : MVIIntent, A : MVIAction> internal constructor(
    public val parallelIntents: Boolean,
    public val actionShareBehavior: ActionShareBehavior,
    public val plugins: List<StorePlugin<S, I, A>>,
    public val reducer: Reducer<S, I, A>,
)
