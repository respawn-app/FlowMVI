package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin

public abstract class AbstractStorePlugin<S : MVIState, I : MVIIntent, A : MVIAction>(
    final override val name: String? = null,
) : StorePlugin<S, I, A> {

    final override fun toString(): String = "StorePlugin \"${name ?: super.toString()}\""
    final override fun hashCode(): Int = name?.hashCode() ?: super.hashCode()
    final override fun equals(other: Any?): Boolean {
        if (other !is StorePlugin<*, *, *>) return false
        if (other.name == null && name == null) return this === other
        return name == other.name
    }
}
