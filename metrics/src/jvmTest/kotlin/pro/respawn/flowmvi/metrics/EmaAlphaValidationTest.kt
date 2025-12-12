package pro.respawn.flowmvi.metrics

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class EmaAlphaValidationTest : FreeSpec({

    configure()

    "Ema rejects alpha <= 0" {
        shouldThrow<IllegalArgumentException> { Ema(0.0) }
        shouldThrow<IllegalArgumentException> { Ema(-0.5) }
    }

    "Ema rejects alpha >= 1" {
        shouldThrow<IllegalArgumentException> { Ema(1.0) }
        shouldThrow<IllegalArgumentException> { Ema(2.0) }
    }

    "PerformanceMetrics rejects alpha <= 0" {
        shouldThrow<IllegalArgumentException> {
            PerformanceMetrics(windowSeconds = 1, emaAlpha = 0.0, bucketDuration = 1.seconds)
        }
    }

    "PerformanceMetrics rejects alpha >= 1" {
        shouldThrow<IllegalArgumentException> {
            PerformanceMetrics(windowSeconds = 1, emaAlpha = 1.0, bucketDuration = 1.seconds)
        }
    }

    "MetricsCollector rejects invalid emaAlpha at construction" {
        val scope = CoroutineScope(EmptyCoroutineContext)
        val clock = MutableClock(Instant.fromEpochMilliseconds(0))
        val ts = MutableTimeSource()
        shouldThrow<IllegalArgumentException> {
            MetricsCollector<TestState, TestIntent, TestAction>(
                reportingScope = scope,
                offloadContext = EmptyCoroutineContext,
                bucketDuration = 1.seconds,
                windowSeconds = 60,
                emaAlpha = 0.0,
                clock = clock,
                timeSource = ts,
            )
        }
    }
})
