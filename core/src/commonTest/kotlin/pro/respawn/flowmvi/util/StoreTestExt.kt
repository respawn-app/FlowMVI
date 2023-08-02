@file:Suppress("Filename")
@file:OptIn(ExperimentalStdlibApi::class)

package pro.respawn.flowmvi.util

import io.kotest.common.ExperimentalKotest
import io.kotest.core.concurrency.CoroutineDispatcherFactory
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.scopes.FreeSpecContainerScope
import io.kotest.core.spec.style.scopes.FreeSpecTerminalScope
import io.kotest.core.test.TestCase
import io.kotest.core.test.testCoroutineScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.withContext
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.Provider
import pro.respawn.flowmvi.api.Store
import kotlin.coroutines.coroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalKotest::class)
fun Spec.asUnconfined() {
    coroutineDispatcherFactory = object : CoroutineDispatcherFactory {
        override suspend fun <T> withDispatcher(testCase: TestCase, f: suspend () -> T): T =
            withContext(coroutineContext + UnconfinedTestDispatcher()) { f() }
    }
}

class StoreTestScope<S : MVIState, I : MVIIntent, A : MVIAction>(
    val provider: Provider<S, I, A>,
    val store: Store<S, I, A>
) : Store<S, I, A> by store, Provider<S, I, A> by provider {

    override fun send(intent: I) = store.send(intent)
    override suspend fun emit(intent: I) = store.send(intent)
    override fun intent(intent: I) = store.send(intent)

    suspend inline infix fun I.resultsIn(state: S) {
        emit(this)
        assertEquals(states.value, state)
    }

    suspend inline fun <reified S> I.resultsIn() {
        emit(this)
        assertIs<S>(states.value)
    }

    suspend inline infix fun I.resultsIn(assertion: () -> Unit) {
        emit(this)
        assertion()
    }

    suspend inline infix fun I.resultsIn(action: A) {
        emit(this)
        assertEquals(action, actions.firstOrNull())
    }
}

suspend inline fun <S : MVIState, I : MVIIntent, A : MVIAction> Store<S, I, A>.test(
    crossinline block: suspend Store<S, I, A>.() -> Unit
) = coroutineScope {
    val job = start(this)
    block()
    job.cancelAndJoin()
}

suspend inline fun <S : MVIState, I : MVIIntent, A : MVIAction> Store<S, I, A>.subscribeAndTest(
    crossinline block: suspend StoreTestScope<S, I, A>.() -> Unit,
) = test {
    coroutineScope {
        subscribe {
            StoreTestScope(this, this@subscribeAndTest).run {
                block()
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)
fun FreeSpecContainerScope.idle() = testCoroutineScheduler.advanceUntilIdle()

@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)
fun FreeSpecTerminalScope.idle() = testCoroutineScheduler.advanceUntilIdle()

@OptIn(ExperimentalCoroutinesApi::class)
fun kotlinx.coroutines.test.TestScope.idle() = advanceUntilIdle()
