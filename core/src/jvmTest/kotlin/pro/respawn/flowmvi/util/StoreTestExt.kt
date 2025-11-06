@file:Suppress("Filename")

package pro.respawn.flowmvi.util

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestScope
import io.kotest.core.test.testCoroutineScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun TestScope.idle() = testCoroutineScheduler.advanceUntilIdle()

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun advanceBy(duration: Duration) {
    val scheduler = currentCoroutineContext()[TestCoroutineScheduler]
        ?: error("Test coroutine scheduler is not available")
    scheduler.advanceTimeBy(duration.inWholeMilliseconds)
    scheduler.runCurrent()
}

fun Spec.configure() {
    coroutineTestScope = true
    isolationMode = IsolationMode.SingleInstance
    timeout = 3.seconds.inWholeMilliseconds
    coroutineDebugProbes = false
    invocationTimeout = 3000L
}
