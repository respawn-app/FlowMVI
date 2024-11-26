package pro.respawn.flowmvi.decorator

import pro.respawn.flowmvi.annotation.NotIntendedForInheritance
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin

@OptIn(NotIntendedForInheritance::class)
public class PluginDecorator<S : MVIState, I : MVIIntent, A : MVIAction> internal constructor(
    override val name: String?,
    internal val onIntent: DecorateValue<S, I, A, I>? = null,
    internal val onState: DecorateState<S, I, A>? = null,
    internal val onAction: DecorateValue<S, I, A, A>? = null,
    internal val onException: DecorateValue<S, I, A, Exception>? = null,
    internal val onStart: Decorate<S, I, A>? = null,
    internal val onSubscribe: DecorateArg<S, I, A, Int>? = null,
    internal val onUnsubscribe: DecorateArg<S, I, A, Int>? = null,
) : StorePlugin<S, I, A> {

    // region contract
    override fun toString(): String = "PluginDecorator for ${name?.let { " \"$it\"" }.orEmpty()}"
    override fun hashCode(): Int = name?.hashCode() ?: super.hashCode()
    override fun equals(other: Any?): Boolean = when {
        other !is PluginDecorator<*, *, *> -> false
        other.name == null && name == null -> this === other
        else -> name == other.name
    }
    //endregion
}
