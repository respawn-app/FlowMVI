package pro.respawn.flowmvi.debugger.core.models

import kotlinx.serialization.Serializable
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.debugger.core.name

@Serializable
internal data class StoreState(
    val name: String,
    val body: String,
) {

    constructor(state: MVIState) : this(state.name, state.toString())
}
