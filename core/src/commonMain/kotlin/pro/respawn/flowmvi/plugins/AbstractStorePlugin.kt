package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin

/**
 * A base class for creating custom [StorePlugin]s. This class is preferred over implementing the interface.
 * Use this class when you want to build reusable plugins, inject dependencies,
 * or want to have the reference to the plugin's instance and use it outside of its regular pipeline.
 * For all other cases, prefer [pro.respawn.flowmvi.dsl.plugin] builder function.
 * @see [StorePlugin]
 * @see [pro.respawn.flowmvi.dsl.plugin]
 */
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
