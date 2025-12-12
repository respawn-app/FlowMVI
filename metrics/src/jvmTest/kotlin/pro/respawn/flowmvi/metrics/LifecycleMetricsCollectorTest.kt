package pro.respawn.flowmvi.metrics

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.dsl.plugin
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds

class LifecycleMetricsCollectorTest : FreeSpec({

    configure()

    "Lifecycle.startCount/stopCount counts multiple starts and stops" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onStop(null)
            onStart()
            onStop(null)
            val snap = collector.snapshot().lifecycle
            snap.startCount shouldBe 2
            snap.stopCount shouldBe 2
        }
    }

    "Lifecycle.stopCount increments even if stopped before start" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStop(null)
            val snap = collector.snapshot().lifecycle
            snap.startCount shouldBe 0
            snap.stopCount shouldBe 1
        }
    }

    "Lifecycle.startCount/stopCount are 0 with no events" {
        testCollectorWithTime { collector, _, _ ->
            val snap = collector.snapshot().lifecycle
            snap.startCount shouldBe 0
            snap.stopCount shouldBe 0
        }
    }

    "Lifecycle.uptimeTotal accumulates across two runs" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            ts.advanceBy(100.milliseconds)
            onStop(null)
            onStart()
            ts.advanceBy(200.milliseconds)
            onStop(null)
            collector.snapshot().lifecycle.uptimeTotal shouldBe 300.milliseconds
        }
    }

    "Lifecycle.uptimeTotal excludes current run when not stopped" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            ts.advanceBy(100.milliseconds)
            onStop(null)
            onStart()
            ts.advanceBy(100.milliseconds)
            collector.snapshot().lifecycle.uptimeTotal shouldBe 100.milliseconds
        }
    }

    "Lifecycle.uptimeTotal is 0 for zero elapsed runs" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onStop(null)
            collector.snapshot().lifecycle.uptimeTotal shouldBe ZERO
        }
    }

    "Lifecycle.lifetimeCurrent reflects elapsed since last start" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            ts.advanceBy(50.milliseconds)
            collector.snapshot().lifecycle.lifetimeCurrent shouldBe 50.milliseconds
        }
    }

    "Lifecycle.lifetimeCurrent is 0 after stop" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            ts.advanceBy(50.milliseconds)
            onStop(null)
            collector.snapshot().lifecycle.lifetimeCurrent shouldBe ZERO
        }
    }

    "Lifecycle.lifetimeCurrent is 0 when never started" {
        testCollectorWithTime { collector, _, _ ->
            collector.snapshot().lifecycle.lifetimeCurrent shouldBe ZERO
        }
    }

    "Lifecycle.lifetimeAvg/lifetimeMedian computed from multiple runs" {
        testCollectorWithTime(emaAlpha = 0.5, childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            ts.advanceBy(100.milliseconds)
            onStop(null)
            onStart()
            ts.advanceBy(300.milliseconds)
            onStop(null)
            val snap = collector.snapshot().lifecycle
            snap.lifetimeAvg shouldBe 200.milliseconds
            snap.lifetimeMedian shouldBe 100.milliseconds
        }
    }

    "Lifecycle.lifetimeAvg/lifetimeMedian for single run equal that duration" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            ts.advanceBy(150.milliseconds)
            onStop(null)
            val snap = collector.snapshot().lifecycle
            snap.lifetimeAvg shouldBe 150.milliseconds
            snap.lifetimeMedian shouldBe 150.milliseconds
        }
    }

    "Lifecycle.lifetimeAvg/lifetimeMedian are 0 with no runs" {
        testCollectorWithTime { collector, _, _ ->
            val snap = collector.snapshot().lifecycle
            snap.lifetimeAvg shouldBe ZERO
            snap.lifetimeMedian shouldBe ZERO
        }
    }

    "Lifecycle.bootstrapAvg/bootstrapMedian record onStart duration" {
        testCollectorWithTime(childFactory = { _, ts ->
            plugin {
                onStart {
                    ts.advanceBy(25.milliseconds)
                }
            }
        }) { collector, _, _ ->
            onStart()
            val snap = collector.snapshot().lifecycle
            snap.bootstrapAvg shouldBe 25.milliseconds
            snap.bootstrapMedian shouldBe 25.milliseconds
        }
    }

    "Lifecycle.bootstrapAvg/bootstrapMedian are 0 for instant start" {
        testCollectorWithTime(childFactory = { _, _ ->
            plugin { onStart { } }
        }) { collector, _, _ ->
            onStart()
            val snap = collector.snapshot().lifecycle
            snap.bootstrapAvg shouldBe ZERO
            snap.bootstrapMedian shouldBe ZERO
        }
    }

    "Lifecycle.bootstrapAvg/bootstrapMedian are 0 when never started" {
        testCollectorWithTime { collector, _, _ ->
            val snap = collector.snapshot().lifecycle
            snap.bootstrapAvg shouldBe ZERO
            snap.bootstrapMedian shouldBe ZERO
        }
    }
})
