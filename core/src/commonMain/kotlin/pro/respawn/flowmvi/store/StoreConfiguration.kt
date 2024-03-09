package pro.respawn.flowmvi.store

import kotlinx.coroutines.channels.BufferOverflow
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.logging.StoreLogger
import kotlin.coroutines.CoroutineContext

@FlowMVIDSL
internal data class StoreConfiguration<S : MVIState, I : MVIIntent, A : MVIAction>(
    val initial: S,
    val name: String?,
    val parallelIntents: Boolean,
    val actionShareBehavior: ActionShareBehavior,
    val plugins: Set<StorePlugin<S, I, A>>,
    val intentCapacity: Int,
    val onOverflow: BufferOverflow,
    val debuggable: Boolean,
    val coroutineContext: CoroutineContext,
    val logger: StoreLogger,
    val atomicStateUpdates: Boolean,
)
