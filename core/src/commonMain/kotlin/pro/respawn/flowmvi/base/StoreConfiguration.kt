package pro.respawn.flowmvi.base

import pro.respawn.flowmvi.MVIIntent
import pro.respawn.flowmvi.MVIState

public data class StoreConfiguration<S : MVIState, I : MVIIntent> internal constructor(
    val parallelIntents: Boolean,
    val plugins: List<StorePlugin<S, I>>,
    val reducer: Reducer<S, I>,
)
