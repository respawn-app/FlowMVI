package pro.respawn.flowmvi.impl.decorator

import pro.respawn.flowmvi.annotation.NotIntendedForInheritance
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.decorator.DecoratorContext
import pro.respawn.flowmvi.decorator.StoreDecorator
import pro.respawn.flowmvi.decorator.proceed

@OptIn(NotIntendedForInheritance::class)
internal class DecoratorInstance<S : MVIState, I : MVIIntent, A : MVIAction> internal constructor(
    override val name: String?,
    internal val onIntent: (suspend DecoratorContext<S, I, A, I>.(I) -> I?)? = null,
    internal val onState: (suspend DecoratorContext<S, I, A, S>.(S, S) -> S?)? = null,
    internal val onAction: (suspend DecoratorContext<S, I, A, A>.(A) -> A?)? = null,
    internal val onException: (suspend DecoratorContext<S, I, A, Exception>.(Exception) -> Exception?)? = null,
    internal val onStart: (suspend DecoratorContext<S, I, A, Unit>.() -> Unit)? = null,
    internal val onSubscribe: (suspend DecoratorContext<S, I, A, Unit>.(subs: Int) -> Unit)? = null,
    internal val onUnsubscribe: (suspend DecoratorContext<S, I, A, Unit>.(subs: Int) -> Unit)? = null
) : StoreDecorator<S, I, A> {

    override suspend fun DecoratorContext<S, I, A, Unit>.onStart(): Unit = onStart?.invoke(this) ?: proceed()

    override suspend fun DecoratorContext<S, I, A, I>.onIntent(
        intent: I
    ): I? = onIntent?.invoke(this, intent) ?: proceed(intent)

    override suspend fun DecoratorContext<S, I, A, S>.onState(
        old: S,
        new: S
    ): S? = onState?.invoke(this, old, new) ?: proceed(new)

    override suspend fun DecoratorContext<S, I, A, A>.onAction(
        action: A
    ): A? = onAction?.invoke(this, action) ?: proceed(action)

    override suspend fun DecoratorContext<S, I, A, Exception>.onException(
        e: Exception
    ): Exception? = onException?.invoke(this, e) ?: proceed(e)

    override suspend fun DecoratorContext<S, I, A, Unit>.onSubscribe(
        newSubscriberCount: Int
    ) = onSubscribe?.invoke(this, newSubscriberCount) ?: proceed()

    override suspend fun DecoratorContext<S, I, A, Unit>.onUnsubscribe(
        newSubscriberCount: Int
    ) = onUnsubscribe?.invoke(this, newSubscriberCount) ?: proceed()

    // region contract
    override fun toString(): String = "StoreDecorator${name?.let { " \"$it\"" }.orEmpty()}"
    override fun hashCode(): Int = name?.hashCode() ?: super.hashCode()
    override fun equals(other: Any?): Boolean = when {
        other !is DecoratorInstance<*, *, *> -> false
        other.name == null && name == null -> this === other
        else -> name == other.name
    }
    //endregion
}
