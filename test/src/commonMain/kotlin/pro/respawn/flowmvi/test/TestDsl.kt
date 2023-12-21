package pro.respawn.flowmvi.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.withTimeout
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.api.Store
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * A class which implements a dsl for testing [Store].
 */
public class StoreTestScope<S : MVIState, I : MVIIntent, A : MVIAction> @PublishedApi internal constructor(
    public val provider: Provider<S, I, A>,
    public val store: Store<S, I, A>,
    public val timeoutMs: Long = 3000L,
) : Store<S, I, A> by store, Provider<S, I, A> by provider {

    @OptIn(DelicateStoreApi::class)
    override val state: S by store::state
    override fun send(intent: I): Unit = store.send(intent)
    override suspend fun emit(intent: I): Unit = store.send(intent)
    override fun intent(intent: I): Unit = store.send(intent)

    /**
     * Assert that [Provider.state] is equal to [state]
     */
    public suspend inline infix fun I.resultsIn(state: S) {
        emit(this)
        assertEquals(states.value, state, "Expected state to be $state but got ${states.value}")
    }

    /**
     * Assert that [Provider.state]'s state is of type [S]
     */
    public suspend inline fun <reified S> I.resultsIn() {
        emit(this)
        assertIs<S>(states.value)
    }

    /**
     * Assert that intent [this] passes checks defined in [assertion]
     */
    public suspend inline infix fun I.resultsIn(assertion: () -> Unit) {
        emit(this)
        assertion()
    }

    /**
     * Assert that intent [this] results in [action]
     */
    public suspend inline infix fun I.resultsIn(action: A) {
        emit(this)
        withTimeout(timeoutMs) {
            assertEquals(action, actions.firstOrNull())
        }
    }
}

/**
 * Call [Store.start] and then execute [block], cancelling the store afterwards
 */
public suspend inline fun <S : MVIState, I : MVIIntent, A : MVIAction> Store<S, I, A>.test(
    crossinline block: suspend Store<S, I, A>.(job: Job) -> Unit
): Job = coroutineScope {
    val job = start(this)
    block(job)
    job.apply { cancelAndJoin() }
}

/**
 * Call [Store.start], then call [Store.subscribe] and execute [block] inside that scope.
 * Unsubscribe and stop the store afterwards
 */
public suspend inline fun <S : MVIState, I : MVIIntent, A : MVIAction> Store<S, I, A>.subscribeAndTest(
    crossinline block: suspend StoreTestScope<S, I, A>.() -> Unit,
): Job = test {
    coroutineScope {
        subscribe {
            StoreTestScope(this, this@subscribeAndTest).run { block() }
        }
    }
}

/**
 * Alias for [TestScope.advanceUntilIdle].
 */
@OptIn(ExperimentalCoroutinesApi::class)
public fun TestScope.wait(): Unit = advanceUntilIdle()
