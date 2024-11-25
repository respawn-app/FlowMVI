package pro.respawn.flowmvi.decorators

import kotlinx.atomicfu.atomic
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.decorator.StoreDecorator
import pro.respawn.flowmvi.decorator.decorator
import pro.respawn.flowmvi.decorator.ignore
import pro.respawn.flowmvi.dsl.StoreBuilder

@FlowMVIDSL
@ExperimentalFlowMVIAPI
public fun <S : MVIState, I : MVIIntent, A : MVIAction> conflateDecorator(
    compareActions: ((it: A, other: A) -> Boolean)?,
    compareIntents: ((it: I, other: I) -> Boolean)?
): StoreDecorator<S, I, A> = decorator {
    if (compareIntents != null) {
        val lastIntent = atomic<I?>(null)
        onIntent { cur ->
            val prev = lastIntent.getAndSet(cur)
            if (prev != null && compareIntents(prev, cur)) return@onIntent ignore()
            proceed(cur)
        }
    }
    if (compareActions != null) {
        val lastAction = atomic<A?>(null)
        onAction { action ->
            val prev = lastAction.getAndSet(action)
            if (prev != null && compareActions(prev, action)) return@onAction ignore()
            proceed(action)
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
