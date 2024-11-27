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

private class Conflated<T : Any> {

    private val value = atomic<T?>(null)

    fun update(with: T?) = value.getAndSet(with)
}

/**
 * Conflates intents and actions going through this decorator based on the equality function provided in
 * [compareIntents] and [compareActions].
 *
 * A default implementation uses a structural equality check.
 *
 * The first event (intent/action) since the store has started will never be dropped, then
 * if the compare function returns true for the last sent event and the current one, the event will be dropped.
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> conflateDecorator(
    compareActions: ((it: A, other: A) -> Boolean)?,
    compareIntents: ((it: I, other: I) -> Boolean)?
): PluginDecorator<S, I, A> = decorator {
    val lastIntent = Conflated<I>()
    if (compareIntents != null) {
        onIntent { chain, cur ->
            val prev = lastIntent.update(cur)
            if (prev != null && compareIntents(prev, cur)) return@onIntent null
            with(chain) { onIntent(cur) }
        }
    }
    val lastAction = Conflated<A>()
    if (compareActions != null) {
        onAction { chain, cur ->
            val prev = lastAction.update(cur)
            if (prev != null && compareActions(prev, cur)) return@onAction null
            with(chain) { onAction(cur) }
        }
    }
    onStop { child, e ->
        lastIntent.update(null)
        lastAction.update(null)
        child.run { onStop(e) }
    }
}

/**
 * Returns a new [conflateDecorator] for all intents in this store.
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.conflateIntents(
    compare: (it: I, other: I) -> Boolean = { a, b -> a == b },
): Unit = install(conflateDecorator(null, compare))

/**
 * Installs a new [conflateDecorator] for all actions in this store.
 */
@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.conflateActions(
    compare: (it: A, other: A) -> Boolean = { a, b -> a == b },
): Unit = install(conflateDecorator(compare, null))
