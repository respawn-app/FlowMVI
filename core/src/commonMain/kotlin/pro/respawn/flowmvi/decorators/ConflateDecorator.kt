package pro.respawn.flowmvi.decorators

import kotlinx.atomicfu.atomic
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.decorator.PluginDecorator
import pro.respawn.flowmvi.decorator.decorator
import pro.respawn.flowmvi.dsl.StoreBuilder

@PublishedApi
internal class Conflated<T : Any> {

    private val value = atomic<T?>(null)

    fun update(with: T?) = value.getAndSet(with)
}

/**
 * Conflates intents going through this decorator based on the equality function provided in
 * [compare].
 *
 * The default implementation uses a structural equality check.
 *
 * The first intent since the store has started will never be dropped, then
 * if the compare function returns true for the last sent event and the current one, the event will be dropped.
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> conflateIntentsDecorator(
    name: String? = "ConflateIntents",
    crossinline compare: ((it: I, other: I) -> Boolean) = MVIIntent::equals,
): PluginDecorator<S, I, A> = decorator {
    this.name = name
    val lastIntent = Conflated<I>()
    onIntent { chain, cur ->
        val prev = lastIntent.update(cur)
        if (prev != null && compare(prev, cur)) return@onIntent null
        with(chain) { onIntent(cur) }
    }
    onStop { child, e ->
        lastIntent.update(null)
        child.run { onStop(e) }
    }
}

/**
 * Conflates intents going through this decorator based on the equality function provided in
 * [compare].
 *
 * The default implementation uses a structural equality check.
 *
 * The first intent since the store has started will never be dropped, then
 * if the compare function returns true for the last sent event and the current one, the event will be dropped.
 */
@ExperimentalFlowMVIAPI
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> conflateActionsDecorator(
    name: String? = "ConflateActions",
    crossinline compare: ((it: A, other: A) -> Boolean) = MVIAction::equals,
): PluginDecorator<S, I, A> = decorator {
    this.name = name
    val lastAction = Conflated<A>()
    onAction { chain, cur ->
        val prev = lastAction.update(cur)
        if (prev != null && compare(prev, cur)) return@onAction null
        with(chain) { onAction(cur) }
    }
    onStop { child, e ->
        lastAction.update(null)
        child.run { onStop(e) }
    }
}

/**
 * Returns a new [conflateIntentsDecorator] for all intents in this store.
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.conflateIntents(
    name: String? = "ConflateIntents",
    crossinline compare: (it: I, other: I) -> Boolean = MVIIntent::equals,
): Unit = install(conflateIntentsDecorator(name, compare))

/**
 * Installs a new [conflateActionsDecorator] for all actions in this store.
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.conflateActions(
    name: String? = "ConflateActions",
    crossinline compare: (it: A, other: A) -> Boolean = MVIAction::equals,
): Unit = install(conflateActionsDecorator(name, compare))
