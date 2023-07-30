package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.channels.BufferOverflow
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.plugins.CompositePlugin

@FlowMVIDSL
internal data class StoreConfiguration<S : MVIState, I : MVIIntent, A : MVIAction>(
    val initial: S,
    val name: String?,
    val parallelIntents: Boolean,
    val actionShareBehavior: ActionShareBehavior,
    val plugin: CompositePlugin<S, I, A>,
    val intentCapacity: Int,
    val onOverflow: BufferOverflow,
)
