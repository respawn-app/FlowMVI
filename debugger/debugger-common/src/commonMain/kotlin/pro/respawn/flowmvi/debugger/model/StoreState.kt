package pro.respawn.flowmvi.debugger.model

import kotlinx.serialization.Serializable
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.debugger.name

@Serializable
data class StoreState(
    val name: String,
    val body: String,
) {

    constructor(state: MVIState) : this(state.name, state.toString())
}
