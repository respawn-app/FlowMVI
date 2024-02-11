package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin

/**
 * A base class for creating custom [StorePlugin]s.
 *
 * It is preferred to use composition instead of inheriting this class.
 * Prefer [pro.respawn.flowmvi.dsl.plugin] builder function instead of extending this class.
 * For an example, see how a [jobManagerPlugin] ([JobManager]) is implemented.
 *
 * @see [StorePlugin]
 * @see [pro.respawn.flowmvi.dsl.plugin]
 */
@Deprecated(
    """
Plugin builders provide sufficient functionality to use them instead of this class.
Extending this class limits your API and leaks lifecycle methods of a plugin to external code.   
This class will become internal in future releases of the library.
"""
)
public abstract class AbstractStorePlugin<S : MVIState, I : MVIIntent, A : MVIAction>(
    final override val name: String? = null,
) : StorePlugin<S, I, A> {

    final override fun toString(): String = "StorePlugin \"${name ?: super.toString()}\""
    final override fun hashCode(): Int = name?.hashCode() ?: super.hashCode()
    final override fun equals(other: Any?): Boolean = when {
        other !is StorePlugin<*, *, *> -> false
        other.name == null && name == null -> this === other
        else -> name == other.name
    }
}
