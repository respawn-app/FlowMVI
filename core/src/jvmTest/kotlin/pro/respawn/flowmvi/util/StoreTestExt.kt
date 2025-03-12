@file:Suppress("Filename")

package pro.respawn.flowmvi.util

import io.kotest.common.ExperimentalKotest
import io.kotest.common.KotestInternal
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.scopes.FreeSpecContainerScope
import io.kotest.core.spec.style.scopes.FreeSpecTerminalScope
import io.kotest.core.test.TestCase
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.engine.coroutines.CoroutineDispatcherFactory
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withContext

@OptIn(ExperimentalKotest::class, KotestInternal::class)
fun Spec.asUnconfined() {
    coroutineTestScope = true
    // coroutineDebugProbes = false
    coroutineDispatcherFactory = object : CoroutineDispatcherFactory {
        override suspend fun <T> withDispatcher(spec: Spec, f: suspend () -> T): T =
            withContext(currentCoroutineContext() + UnconfinedTestDispatcher()) { f() }

        override suspend fun <T> withDispatcher(testCase: TestCase, f: suspend () -> T): T =
            withContext(currentCoroutineContext() + UnconfinedTestDispatcher()) { f() }
    }
}

fun FreeSpecContainerScope.idle() = testCoroutineScheduler.advanceUntilIdle()

fun FreeSpecTerminalScope.idle() = testCoroutineScheduler.advanceUntilIdle()
