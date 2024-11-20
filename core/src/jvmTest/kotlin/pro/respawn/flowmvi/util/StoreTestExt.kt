@file:Suppress("Filename")

package pro.respawn.flowmvi.util

import io.kotest.common.ExperimentalKotest
import io.kotest.core.concurrency.CoroutineDispatcherFactory
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.scopes.FreeSpecContainerScope
import io.kotest.core.spec.style.scopes.FreeSpecTerminalScope
import io.kotest.core.test.TestCase
import io.kotest.core.test.testCoroutineScheduler
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withContext

@OptIn(ExperimentalKotest::class)
fun Spec.asUnconfined() {
    dispatcherAffinity = true
    coroutineTestScope = true
    coroutineDebugProbes = false
    coroutineDispatcherFactory = object : CoroutineDispatcherFactory {
        override suspend fun <T> withDispatcher(testCase: TestCase, f: suspend () -> T): T =
            withContext(currentCoroutineContext() + UnconfinedTestDispatcher()) { f() }
    }
}

fun FreeSpecContainerScope.idle() = testCoroutineScheduler.advanceUntilIdle()

fun FreeSpecTerminalScope.idle() = testCoroutineScheduler.advanceUntilIdle()
