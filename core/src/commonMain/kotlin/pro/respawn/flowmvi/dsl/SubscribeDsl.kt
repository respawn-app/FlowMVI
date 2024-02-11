package pro.respawn.flowmvi.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pro.respawn.flowmvi.api.ActionConsumer
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.ImmutableStore
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.api.StateConsumer
import pro.respawn.flowmvi.api.Store

/**
 * Subscribe to [this] store and suspend until [consume] finishes (which should never return).
 * This means the function will suspend forever.
 * @see subscribe for non-suspending variant
 */
@FlowMVIDSL
public suspend inline fun <S : MVIState, I : MVIIntent, A : MVIAction> ImmutableStore<S, I, A>.collect(
    @BuilderInference crossinline consume: suspend Provider<S, I, A>.() -> Unit,
): Unit = coroutineScope {
    subscribe {
        consume()
    }.join()
}

/**
 * Subscribe to the [store] and invoke [consume] and [render] in parallel in the provided scope.
 *
 * * This function does **not** handle the lifecycle of the UI layer. For that, see platform implementations.
 * * Store's subscribers will **not** wait until the store is launched when they subscribe to the store.
 *   Such subscribers will not receive state updates or actions. Don't forget to launch the store.
 * @see [Store.subscribe]
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> CoroutineScope.subscribe(
    store: ImmutableStore<S, I, A>,
    crossinline consume: suspend (action: A) -> Unit,
    crossinline render: suspend (state: S) -> Unit,
): Job = with(store) {
    subscribe outer@{
        coroutineScope inner@{
            launch {
                actions.collect { consume(it) }
            }
            launch {
                states.collect { render(it) }
            }
        }
    }
}

/**
 * Subscribe to the [store] and invoke [render]. This does not collect store's actions.
 *
 * * This function does **not** handle the lifecycle of the UI layer. For that, see platform implementations.
 * * Store's subscribers will **not** wait until the store is launched when they subscribe to the store.
 *   Such subscribers will not receive state updates or actions. Don't forget to launch the store.
 * @see [Store.subscribe]
 */
@FlowMVIDSL
public inline fun <S : MVIState, I : MVIIntent, A : MVIAction> CoroutineScope.subscribe(
    store: ImmutableStore<S, I, A>,
    crossinline render: suspend (state: S) -> Unit,
): Job = with(store) {
    subscribe {
        states.collect { render(it) }
    }
}

/**
 * Subscribe to the [store] and invoke [ActionConsumer.consume] and [StateConsumer.render] in parallel in the provided scope.
 *
 * * This function does **not** handle the lifecycle of the UI layer. For that, see platform implementations.
 * * Store's subscribers will **not** wait until the store is launched when they subscribe to the store.
 *   Such subscribers will not receive state updates or actions. Don't forget to launch the store.
 * @see [Store.subscribe]
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction, T> T.subscribe(
    store: ImmutableStore<S, I, A>,
    scope: CoroutineScope
): Job where T : ActionConsumer<A>, T : StateConsumer<S> = with(scope) {
    subscribe(store, ::consume, ::render)
}

/**
 * Subscribe to the [store] and invoke [StateConsumer.render] in the provided scope.
 *
 * * This function does **not** handle the lifecycle of the UI layer. For that, see platform implementations.
 * * Store's subscribers will **not** wait until the store is launched when they subscribe to the store.
 *   Such subscribers will not receive state updates or actions. Don't forget to launch the store.
 * @see [Store.subscribe]
 */
@FlowMVIDSL
public fun <S : MVIState, I : MVIIntent, A : MVIAction> StateConsumer<S>.subscribe(
    store: ImmutableStore<S, I, A>,
    scope: CoroutineScope
): Job = with(scope) { subscribe(store, ::render) }
