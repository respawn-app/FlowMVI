package pro.respawn.flowmvi.metrics

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.dsl.plugin
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds

class ExceptionMetricsCollectorTest : FreeSpec({

    configure()

    "Exceptions.total increments on each exception observed" {
        testCollectorWithTime(childFactory = { _, _ ->
            plugin {
                onException { null }
            }
        }) { collector, _, _ ->
            onStart()
            onException(IllegalStateException("a"))
            onException(IllegalStateException("b"))
            collector.snapshot().exceptions.total shouldBe 2
        }
    }

    "Exceptions.total is 0 when none recorded" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().exceptions.total shouldBe 0
        }
    }

    "Exceptions.total counts both handled and unhandled" {
        testCollectorWithTime(childFactory = { _, _ ->
            var call = 0
            plugin {
                onException { e ->
                    call++
                    if (call == 1) null else e
                }
            }
        }) { collector, _, _ ->
            onStart()
            onException(IllegalStateException("a"))
            onException(IllegalStateException("b"))
            collector.snapshot().exceptions.total shouldBe 2
        }
    }

    "Exceptions.handled increments when plugin handles exception (returns null)" {
        testCollectorWithTime(childFactory = { _, _ ->
            plugin { onException { null } }
        }) { collector, _, _ ->
            onStart()
            onException(IllegalStateException("a"))
            collector.snapshot().exceptions.handled shouldBe 1
        }
    }

    "Exceptions.handled does not increment when exception unhandled (returns non-null)" {
        testCollectorWithTime(childFactory = { _, _ ->
            plugin { onException { e -> e } }
        }) { collector, _, _ ->
            onStart()
            onException(IllegalStateException("a"))
            collector.snapshot().exceptions.handled shouldBe 0
        }
    }

    "Exceptions.handled is 0 with no exceptions" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().exceptions.handled shouldBe 0
        }
    }

    "Exceptions.recoveryLatencyAvg/Median sample handled recovery durations" {
        testCollectorWithTime(emaAlpha = 0.5, childFactory = { _, ts ->
            var call = 0
            plugin {
                onException {
                    call++
                    ts.advanceBy(if (call <= 2) 10.milliseconds else 20.milliseconds)
                    null
                }
            }
        }) { collector, _, _ ->
            onStart()
            repeat(3) { onException(IllegalStateException("x$it")) }
            val snap = collector.snapshot().exceptions
            snap.recoveryLatencyAvg shouldBe 15.milliseconds
            snap.recoveryLatencyMedian shouldBe 10.milliseconds
        }
    }

    "Exceptions.recoveryLatencyAvg/Median are 0 when only unhandled exceptions" {
        testCollectorWithTime(childFactory = { _, _ ->
            plugin { onException { e -> e } }
        }) { collector, _, _ ->
            onStart()
            onException(IllegalStateException("a"))
            val snap = collector.snapshot().exceptions
            snap.recoveryLatencyAvg shouldBe ZERO
            snap.recoveryLatencyMedian shouldBe ZERO
        }
    }

    "Exceptions.recoveryLatencyAvg/Median stable for identical handled durations" {
        testCollectorWithTime(emaAlpha = 0.5, childFactory = { _, ts ->
            plugin {
                onException {
                    ts.advanceBy(10.milliseconds)
                    null
                }
            }
        }) { collector, _, _ ->
            onStart()
            repeat(2) { onException(IllegalStateException("x$it")) }
            val snap = collector.snapshot().exceptions
            snap.recoveryLatencyAvg shouldBe 10.milliseconds
            snap.recoveryLatencyMedian shouldBe 10.milliseconds
        }
    }
})
