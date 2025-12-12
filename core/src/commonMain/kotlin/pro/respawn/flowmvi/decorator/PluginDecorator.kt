package pro.respawn.flowmvi.decorator

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.StorePlugin

/**
 * This class is an (already created) instance of the decorator for a [StorePlugin].
 *
 * To use this instance, you first need to obtain a child plugin instance and apply the decorator using
 * [StorePlugin.decoratedWith] or [PluginDecorator.decorates].
 *
 * You will get a new instance of [StorePlugin] that will be wrapped by this decorator.
 *
 * See detailed documentation in [DecoratorBuilder] where methods are defined.
 *
 * @see DecoratorBuilder
 */
public data class PluginDecorator<S : MVIState, I : MVIIntent, A : MVIAction> internal constructor(
    /** The name of the decorator. Must be unique or `null` */
    public val name: String?,
    internal val onIntentEnqueue: DecorateValueNonSuspend<S, I, A, I>? = null,
    internal val onIntent: DecorateValue<S, I, A, I>? = null,
    internal val onState: DecorateState<S, I, A>? = null,
    internal val onAction: DecorateValue<S, I, A, A>? = null,
    internal val onActionDispatch: DecorateValueNonSuspend<S, I, A, A>? = null,
    internal val onException: DecorateValue<S, I, A, Exception>? = null,
    internal val onStart: Decorate<S, I, A>? = null,
    internal val onSubscribe: DecorateArg<S, I, A, Int>? = null,
    internal val onUnsubscribe: DecorateArg<S, I, A, Int>? = null,
    internal val onStop: DecorateShutdown<S, I, A, Exception?>? = null,
    internal val onUndeliveredIntent: DecorateShutdown<S, I, A, I>? = null,
    internal val onUndeliveredAction: DecorateShutdown<S, I, A, A>? = null,
) {

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
