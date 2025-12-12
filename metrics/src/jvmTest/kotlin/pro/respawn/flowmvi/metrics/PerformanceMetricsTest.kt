package pro.respawn.flowmvi.metrics

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeNaN
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class PerformanceMetricsTest : FreeSpec({

    "recordOperation updates EMA sequentially and sets baseline" {
        val clock = MutableClock(Clock.System.now())
        val metrics = PerformanceMetrics(windowSeconds = 5, emaAlpha = 0.1, bucketDuration = 1.seconds, clock = clock)

        metrics.recordOperation(10.milliseconds)
        metrics.averageTimeMillis shouldBe 10.0

        metrics.recordOperation(20.milliseconds)

        metrics.averageTimeMillis shouldBe (11.0 plusOrMinus 0.0001)
        metrics.totalOperations shouldBe 2
    }

    "recordOperation handles very large duration without NaN" {
        val clock = MutableClock(Clock.System.now())
        val metrics = PerformanceMetrics(windowSeconds = 3, emaAlpha = 0.2, bucketDuration = 1.seconds, clock = clock)

        metrics.recordOperation(2.days)

        metrics.averageTimeMillis.isFinite() shouldBe true
    }

    "medianTimeMillis returns expected values including bootstrap" {
        val clock = MutableClock(Clock.System.now())
        val metrics = PerformanceMetrics(windowSeconds = 10, emaAlpha = 0.2, bucketDuration = 1.seconds, clock = clock)

        listOf(10, 50, 20, 40, 30).forEach { metrics.recordOperation(it.milliseconds) }
        metrics.medianTimeMillis() shouldBe (30.0 plusOrMinus 0.0001)
    }

    "medianTimeMillis uses sorted bootstrap for fewer samples" {
        val clock = MutableClock(Clock.System.now())
        val metrics = PerformanceMetrics(windowSeconds = 10, emaAlpha = 0.2, bucketDuration = 1.seconds, clock = clock)

        listOf(5, 15, 10).forEach { metrics.recordOperation(it.milliseconds) }
        metrics.medianTimeMillis() shouldBe (10.0 plusOrMinus 0.0001)
    }

    "medianTimeMillis without samples returns NaN" {
        val metrics = PerformanceMetrics(windowSeconds = 3, emaAlpha = 0.1, bucketDuration = 1.seconds)

        metrics.medianTimeMillis().shouldBeNaN()
    }

    "opsPerSecond counts operations within window" {
        val clock = MutableClock(Clock.System.now())
        val metrics = PerformanceMetrics(windowSeconds = 5, emaAlpha = 0.1, bucketDuration = 1.seconds, clock = clock)

        repeat(5) { metrics.recordOperation(1.milliseconds) }

        metrics.opsPerSecond() shouldBe (1.0 plusOrMinus 1e-9)
    }

    "opsPerSecond accounts for sub-second bucketDuration" {
        val clock = MutableClock(Clock.System.now())
        val metrics = PerformanceMetrics(windowSeconds = 10, emaAlpha = 0.1, bucketDuration = 200.milliseconds, clock = clock)

        repeat(10) { metrics.recordOperation(1.milliseconds) }

        metrics.opsPerSecond() shouldBe (5.0 plusOrMinus 1e-9)
    }

    "opsPerSecond accounts for multi-second bucketDuration" {
        val clock = MutableClock(Clock.System.now())
        val metrics = PerformanceMetrics(windowSeconds = 5, emaAlpha = 0.1, bucketDuration = 2.seconds, clock = clock)

        repeat(5) { metrics.recordOperation(1.milliseconds) }

        metrics.opsPerSecond() shouldBe (0.5 plusOrMinus 1e-9)
    }

    "opsPerSecond resets after large time jump beyond window" {
        val clock = MutableClock(Clock.System.now())
        val metrics = PerformanceMetrics(windowSeconds = 4, emaAlpha = 0.1, bucketDuration = 1.seconds, clock = clock)

        repeat(3) { metrics.recordOperation(1.milliseconds) }

        clock.advanceBy(6.seconds)
        metrics.opsPerSecond() shouldBe 0.0
        metrics.stateForTest().buckets.all { it == 0 } shouldBe true
    }

    "opsPerSecond handles rapid bucket rolls without negative indices" {
        val clock = MutableClock(Clock.System.now())
        val metrics = PerformanceMetrics(windowSeconds = 4, emaAlpha = 0.1, bucketDuration = 1.seconds, clock = clock)

        repeat(10) {
            clock.advanceBy(1.seconds)
            metrics.opsPerSecond()
        }

        metrics.stateForTest().bucketIndex.shouldBeExactly(10 % 4)
    }

    "advanceBuckets moves exactly one slot after bucketDuration" {
        val clock = MutableClock(Clock.System.now())
        val metrics = PerformanceMetrics(windowSeconds = 3, emaAlpha = 0.1, bucketDuration = 1.seconds, clock = clock)

        val startIndex = metrics.stateForTest().bucketIndex
        clock.advanceBy(1.seconds)
        metrics.opsPerSecond()

        metrics.stateForTest().bucketIndex shouldBeExactly (startIndex + 1) % 3
    }

    "advanceBuckets past full window clears buckets" {
        val clock = MutableClock(Clock.System.now())
        val metrics = PerformanceMetrics(windowSeconds = 3, emaAlpha = 0.1, bucketDuration = 1.seconds, clock = clock)

        repeat(2) { metrics.recordOperation(1.milliseconds) }
        clock.advanceBy(5.seconds)
        metrics.opsPerSecond()

        metrics.stateForTest().buckets.toList().shouldContainExactly(0, 0, 0)
    }

    "advanceBuckets with no elapsed time keeps index" {
        val clock = MutableClock(Clock.System.now())
        val metrics = PerformanceMetrics(windowSeconds = 3, emaAlpha = 0.1, bucketDuration = 1.seconds, clock = clock)

        val before = metrics.stateForTest().bucketIndex
        metrics.opsPerSecond()
        metrics.stateForTest().bucketIndex shouldBeExactly before
    }

    "reset clears counters, ema and quantiles" {
        val clock = MutableClock(Clock.System.now())
        val metrics = PerformanceMetrics(windowSeconds = 3, emaAlpha = 0.3, bucketDuration = 1.seconds, clock = clock)

        repeat(3) { metrics.recordOperation(10.milliseconds) }
        metrics.opsPerSecond()

        metrics.reset()

        metrics.totalOperations shouldBe 0
        metrics.averageTimeMillis.shouldBeNaN()
        metrics.stateForTest().apply {
            bucketIndex shouldBeExactly 0
            buckets.toList().shouldContainExactly(0, 0, 0)
            totalOperations shouldBe 0
            emaMillis shouldBe 0.0
        }
        metrics.medianTimeMillis().shouldBeNaN()
    }

    "reset after no samples leaves default state" {
        val metrics = PerformanceMetrics(windowSeconds = 2, emaAlpha = 0.1, bucketDuration = 1.seconds)

        metrics.reset()

        metrics.totalOperations shouldBe 0
        metrics.averageTimeMillis.shouldBeNaN()
        metrics.stateForTest().buckets.all { it == 0 } shouldBe true
    }

    "parameter validation rejects non positive window and bucket duration" {
        shouldThrow<IllegalArgumentException> { PerformanceMetrics(windowSeconds = 0) }
        shouldThrow<IllegalArgumentException> { PerformanceMetrics(windowSeconds = 1, bucketDuration = Duration.ZERO) }
    }
})
