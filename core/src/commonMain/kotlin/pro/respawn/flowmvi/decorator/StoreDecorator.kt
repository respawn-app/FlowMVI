package pro.respawn.flowmvi.decorator

import pro.respawn.flowmvi.annotation.NotIntendedForInheritance
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState

@OptIn(ExperimentalSubclassOptIn::class)
@Suppress("ComplexInterface")
@SubclassOptInRequired(NotIntendedForInheritance::class)
public interface StoreDecorator<S : MVIState, I : MVIIntent, A : MVIAction> {

    public val name: String?

    public suspend fun DecoratorContext<S, I, A, Unit>.onStart()

    public suspend fun DecoratorContext<S, I, A, I>.onIntent(intent: I): I?

    public suspend fun DecoratorContext<S, I, A, S>.onState(old: S, new: S): S?

    public suspend fun DecoratorContext<S, I, A, A>.onAction(action: A): A?

    public suspend fun DecoratorContext<S, I, A, Exception>.onException(e: Exception): Exception?

    public suspend fun DecoratorContext<S, I, A, Int>.onSubscribe(newSubscriberCount: Int)

    public suspend fun DecoratorContext<S, I, A, Int>.onUnsubscribe(newSubscriberCount: Int)

    override fun toString(): String
    override fun hashCode(): Int
    override fun equals(other: Any?): Boolean
}
