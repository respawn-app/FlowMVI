package pro.respawn.flowmvi.debugger.model

import kotlinx.serialization.Serializable
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.debugger.name

/**
 * The remote state of the store
 * @param name the label to display
 * @param body string representation of the state
 */
@Serializable
public data class StoreState(
    val name: String,
    val body: String,
) {

    public constructor(state: MVIState) : this(state.name, state.toString())
}
