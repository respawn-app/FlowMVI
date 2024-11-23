package pro.respawn.flowmvi.decorator

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

public class StoreDecorator<S : MVIState, I : MVIIntent, A : MVIAction> internal constructor(
    public val name: String?,
    internal val wrapIntent: (suspend DecoratorContext<S, I, A, I>.(I) -> I?)? = null,
    internal val wrapState: (suspend DecoratorContext<S, I, A, S>.(S, S) -> S?)? = null,
    internal val wrapAction: (suspend DecoratorContext<S, I, A, A>.(A) -> A?)? = null,
    internal val wrapException: (suspend DecoratorContext<S, I, A, Exception>.(Exception) -> Exception?)? = null,
    internal val wrapStart: (suspend DecoratorContext<S, I, A, Unit>.() -> Unit)? = null,
    internal val wrapSubscribe: (suspend DecoratorContext<S, I, A, Unit>.() -> Unit)? = null,
    internal val wrapUnsubscribe: (suspend DecoratorContext<S, I, A, Unit>.() -> Unit)? = null
) {

    public suspend fun DecoratorContext<S, I, A, I>.wrapIntent(
        intent: I
    ): I? = wrapIntent?.invoke(this, intent) ?: proceed(intent)

    public suspend fun DecoratorContext<S, I, A, S>.wrapState(
        old: S,
        new: S
    ): S? = wrapState?.invoke(this, old, new) ?: proceed(new)

    public suspend fun DecoratorContext<S, I, A, A>.wrapAction(
        action: A
    ): A? = wrapAction?.invoke(this, action) ?: proceed(action)

    public suspend fun DecoratorContext<S, I, A, Exception>.wrapException(
        e: Exception
    ): Exception? = wrapException?.invoke(this, e) ?: proceed(e)

    public suspend fun DecoratorContext<S, I, A, Unit>.wrapStart(): Unit = wrapStart?.invoke(this) ?: proceed()

    public suspend fun DecoratorContext<S, I, A, Unit>.wrapSubscribe(): Unit = wrapSubscribe?.invoke(this) ?: proceed()

    public suspend fun DecoratorContext<S, I, A, Unit>.wrapUnsubscribe(): Unit =
        wrapUnsubscribe?.invoke(this) ?: proceed()

    // region contract
    override fun toString(): String = "StoreDecorator${name?.let { " \"$it\"" }.orEmpty()}"
    override fun hashCode(): Int = name?.hashCode() ?: super.hashCode()
    override fun equals(other: Any?): Boolean = when {
        other !is StoreDecorator<*, *, *> -> false
        other.name == null && name == null -> this === other
        else -> name == other.name
    }
    //endregion
}
