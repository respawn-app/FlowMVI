@file:Suppress("Filename")

package pro.respawn.flowmvi.util

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestScope
import io.kotest.core.test.testCoroutineScheduler
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun TestScope.idle() = testCoroutineScheduler.advanceUntilIdle()

fun TestScope.advanceBy(duration: Duration) = testCoroutineScheduler.run {
    advanceTimeBy(duration)
    runCurrent()
}

fun Spec.configure() {
    coroutineTestScope = true
    isolationMode = IsolationMode.SingleInstance
    timeout = 3.seconds.inWholeMilliseconds
    coroutineDebugProbes = false
    invocationTimeout = 3000L
}
