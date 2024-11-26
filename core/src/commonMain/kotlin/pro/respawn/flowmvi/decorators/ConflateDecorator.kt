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

    fun update(with: T) = value.getAndSet(with)
}

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> conflateDecorator(
    compareActions: ((it: A, other: A) -> Boolean)?,
    compareIntents: ((it: I, other: I) -> Boolean)?
): PluginDecorator<S, I, A> = decorator {
    if (compareIntents != null) {
        val lastIntent = Conflated<I>()
        onIntent { chain, cur ->
            val prev = lastIntent.update(cur)
            if (prev != null && compareIntents(prev, cur)) return@onIntent null
            with(chain) { onIntent(cur) }
        }
    }
    if (compareActions != null) {
        val lastAction = Conflated<A>()
        onAction { chain, cur ->
            val prev = lastAction.update(cur)
            if (prev != null && compareActions(prev, cur)) return@onAction null
            with(chain) { onAction(cur) }
        }
    }
}

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.conflateIntents(
    compare: (it: I, other: I) -> Boolean = { a, b -> a == b },
): Unit = install(conflateDecorator(null, compare))

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.conflateActions(
    compare: (it: A, other: A) -> Boolean = { a, b -> a == b },
): Unit = install(conflateDecorator(compare, null))
