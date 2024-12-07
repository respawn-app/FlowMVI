package pro.respawn.flowmvi.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.lifecycle.StoreLifecycle
import pro.respawn.flowmvi.dsl.collect
import kotlin.test.assertTrue

/**
 * Call [Store.start] and then execute [block], cancelling the store afterwards
 */
public suspend inline fun <S : MVIState, I : MVIIntent, A : MVIAction> Store<S, I, A>.test(
    crossinline block: suspend Store<S, I, A>.() -> Unit
): Unit = coroutineScope {
    try {
        start(this)
        block()
    } finally {
        closeAndWait()
    }
}

/**
 * Call [Store.start], then call [Store.subscribe] and execute [block] inside that scope.
 * Unsubscribe and stop the store afterwards
 */
public suspend inline fun <S : MVIState, I : MVIIntent, A : MVIAction> Store<S, I, A>.subscribeAndTest(
    crossinline block: suspend StoreTestScope<S, I, A>.() -> Unit,
): Unit = test {
    collect {
        StoreTestScope(this, this@subscribeAndTest).run { block() }
    }
}

/**
 * Alias for [TestScope.advanceUntilIdle].
 */
@OptIn(ExperimentalCoroutinesApi::class)
public fun TestScope.wait(): Unit = advanceUntilIdle()

public fun StoreLifecycle.ensureStarted(): Unit = assertTrue(
    isStarted,
    "Store is closed! Ensure that you do not attempt to use the store after it has been closed."
)
