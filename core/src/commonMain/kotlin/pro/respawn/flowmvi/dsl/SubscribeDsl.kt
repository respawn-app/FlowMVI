package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.Consumer
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store

/**
 * Subscribe to the [store] and invoke [consume] and [render] in parallel in the provided scope.
 * This function does **not** handle the lifecycle of the UI layer. For that, see platform implementations.
 * @see [Store.subscribe]
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> CoroutineScope.subscribe(
    store: Store<S, I, A>,
    crossinline consume: suspend (action: A) -> Unit,
    crossinline render: suspend (state: S) -> Unit,
): Job = with(store) {
    subscribe {
        coroutineScope inner@{
            this@inner.launch {
                actions.collect { consume(it) }
            }
            this@inner.launch {
                states.collect { render(it) }
            }
        }
    }
}

/**
 * Subscribe to the [Consumer.container] and invoke [Consumer.consume] and [Consumer.render] in parallel in the provided scope.
 * This function does **not** handle the lifecycle of the UI layer. For that, see platform implementations.
 * @see [Store.subscribe]
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> Consumer<S, I, A>.subscribe(
    scope: CoroutineScope
): Job = with(scope) {
    subscribe(container.store, ::consume, ::render)
}
