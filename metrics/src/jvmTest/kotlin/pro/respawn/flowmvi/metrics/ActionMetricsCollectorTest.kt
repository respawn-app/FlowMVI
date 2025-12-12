package pro.respawn.flowmvi.metrics

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.dsl.plugin
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ActionMetricsCollectorTest : FreeSpec({

    configure()

    "Action.sent increments when onAction returns non-null" {
        testCollectorWithTime(childFactory = { _, ts ->
            plugin {
                onAction { action ->
                    ts.advanceBy(1.milliseconds)
                    action
                }
            }
        }) { collector, _, _ ->
            onStart()
            onAction(TestAction(1))
            collector.snapshot().actions.sent shouldBe 1
        }
    }

    "Action.sent does not increment when onAction returns null" {
        testCollectorWithTime(childFactory = { _, _ ->
            plugin {
                onAction { null }
            }
        }) { collector, _, _ ->
            onStart()
            onAction(TestAction(1))
            collector.snapshot().actions.sent shouldBe 0
        }
    }

    "Action.sent is 0 when no actions emitted" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().actions.sent shouldBe 0
        }
    }

    "Action.delivered increments when onActionDispatch returns non-null" {
        testCollectorWithTime(childFactory = { _, ts ->
            plugin {
                onActionDispatch { action ->
                    ts.advanceBy(5.milliseconds)
                    action
                }
            }
        }) { collector, _, _ ->
            onStart()
            onAction(TestAction(1))
            onActionDispatch(TestAction(1))
            collector.snapshot().actions.delivered shouldBe 1
        }
    }

    "Action.delivered does not increment when dispatch returns null" {
        testCollectorWithTime(childFactory = { _, _ ->
            plugin {
                onActionDispatch { null }
            }
        }) { collector, _, _ ->
            onStart()
            onAction(TestAction(1))
            onActionDispatch(TestAction(1))
            collector.snapshot().actions.delivered shouldBe 0
        }
    }

    "Action.delivered is 0 when no dispatches" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().actions.delivered shouldBe 0
        }
    }

    "Action.undelivered increments on undelivered action" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            onUndeliveredAction(TestAction(1))
            collector.snapshot().actions.undelivered shouldBe 1
        }
    }

    "Action.undelivered counts multiple undelivered actions" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            onUndeliveredAction(TestAction(1))
            onUndeliveredAction(TestAction(2))
            collector.snapshot().actions.undelivered shouldBe 2
        }
    }

    "Action.undelivered is 0 when none undelivered" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().actions.undelivered shouldBe 0
        }
    }

    "Action.opsPerSecond reflects dispatches within window" {
        testCollectorWithTime(windowSeconds = 2, childFactory = { _, ts ->
            plugin {
                onActionDispatch { action ->
                    ts.advanceBy(1.milliseconds)
                    action
                }
            }
        }) { collector, _, _ ->
            onStart()
            repeat(2) {
                onAction(TestAction(it))
                onActionDispatch(TestAction(it))
            }
            collector.snapshot().actions.opsPerSecond shouldBe (1.0 plusOrMinus 1e-9)
        }
    }

    "Action.opsPerSecond is 0 when no actions" {
        testCollectorWithTime(windowSeconds = 2) { collector, _, _ ->
            onStart()
            collector.snapshot().actions.opsPerSecond shouldBe 0.0
        }
    }

    "Action.opsPerSecond drops samples outside window" {
        testCollectorWithTime(windowSeconds = 2, childFactory = { _, ts ->
            plugin {
                onActionDispatch {
                    ts.advanceBy(1.milliseconds)
                    it
                }
            }
        }) { collector, clock, _ ->
            onStart()
            repeat(2) {
                onAction(TestAction(it))
                onActionDispatch(TestAction(it))
            }
            clock.advanceBy(3.seconds)
            collector.snapshot().actions.opsPerSecond shouldBe 0.0
        }
    }

    "Action.deliveryAvg EMA from two dispatch durations is ~15ms" {
        testCollectorWithTime(emaAlpha = 0.5, childFactory = { _, ts ->
            var call = 0
            plugin {
                onActionDispatch { action ->
                    call++
                    ts.advanceBy(if (call == 1) 10.milliseconds else 20.milliseconds)
                    action
                }
            }
        }) { collector, _, _ ->
            onStart()
            onAction(TestAction(1))
            onActionDispatch(TestAction(1))
            onAction(TestAction(2))
            onActionDispatch(TestAction(2))
            collector.snapshot().actions.deliveryAvg shouldBe 15.milliseconds
        }
    }

    "Action.deliveryAvg uses dispatch duration when queue empty" {
        testCollectorWithTime(emaAlpha = 0.5, childFactory = { _, ts ->
            plugin {
                onActionDispatch { action ->
                    ts.advanceBy(10.milliseconds)
                    action
                }
            }
        }) { collector, _, _ ->
            onStart()
            onActionDispatch(TestAction(1))
            collector.snapshot().actions.deliveryAvg shouldBe 10.milliseconds
        }
    }

    "Action.deliveryAvg is 0 when no actions" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().actions.deliveryAvg shouldBe ZERO
        }
    }

    "Action.delivery quantiles return expected P50/P90/P95/P99" {
        testCollectorWithTime(childFactory = { _, ts ->
            var d = 0
            plugin {
                onActionDispatch {
                    d += 10
                    ts.advanceBy(d.milliseconds)
                    it
                }
            }
        }) { collector, _, _ ->
            onStart()
            repeat(5) {
                onAction(TestAction(it))
                onActionDispatch(TestAction(it))
            }
            val snap = collector.snapshot().actions
            snap.deliveryP50 shouldBe 30.milliseconds
            snap.deliveryP90 shouldBe 50.milliseconds
            snap.deliveryP95 shouldBe 50.milliseconds
            snap.deliveryP99 shouldBe 50.milliseconds
        }
    }

    "Action.delivery quantiles for single sample equal that sample" {
        testCollectorWithTime(childFactory = { _, ts ->
            plugin {
                onActionDispatch {
                    ts.advanceBy(10.milliseconds)
                    it
                }
            }
        }) { collector, _, _ ->
            onStart()
            onAction(TestAction(1))
            onActionDispatch(TestAction(1))
            val snap = collector.snapshot().actions
            snap.deliveryP50 shouldBe 10.milliseconds
            snap.deliveryP90 shouldBe 10.milliseconds
            snap.deliveryP95 shouldBe 10.milliseconds
            snap.deliveryP99 shouldBe 10.milliseconds
        }
    }

    "Action.delivery quantiles are 0 when no samples" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            val snap = collector.snapshot().actions
            snap.deliveryP50 shouldBe ZERO
            snap.deliveryP90 shouldBe ZERO
            snap.deliveryP95 shouldBe ZERO
            snap.deliveryP99 shouldBe ZERO
        }
    }

    "Action.queueTimeAvg records emit to dispatch delay" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            onAction(TestAction(1))
            ts.advanceBy(5.milliseconds)
            onActionDispatch(TestAction(1))
            collector.snapshot().actions.queueTimeAvg shouldBe 5.milliseconds
        }
    }

    "Action.queueTimeAvg stays 0 on empty queue dispatch" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onActionDispatch(TestAction(1))
            collector.snapshot().actions.queueTimeAvg shouldBe ZERO
        }
    }

    "Action.queueTimeAvg EMA advances with multiple samples" {
        testCollectorWithTime(emaAlpha = 0.5, childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            onAction(TestAction(1))
            ts.advanceBy(5.milliseconds)
            onActionDispatch(TestAction(1))
            onAction(TestAction(2))
            ts.advanceBy(15.milliseconds)
            onActionDispatch(TestAction(2))
            collector.snapshot().actions.queueTimeAvg shouldBe 10.milliseconds
        }
    }

    "Action.queueTimeMedian returns quantile from delays" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            onAction(TestAction(1))
            ts.advanceBy(10.milliseconds)
            onActionDispatch(TestAction(1))
            collector.snapshot().actions.queueTimeMedian shouldBe 10.milliseconds
        }
    }

    "Action.queueTimeMedian for single sample equals that delay" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            onAction(TestAction(1))
            ts.advanceBy(7.milliseconds)
            onActionDispatch(TestAction(1))
            collector.snapshot().actions.queueTimeMedian shouldBe 7.milliseconds
        }
    }

    "Action.queueTimeMedian is 0 when no samples" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            collector.snapshot().actions.queueTimeMedian shouldBe ZERO
        }
    }

    "Action.bufferMaxOccupancy tracks queue + inFlight" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onAction(TestAction(1))
            onAction(TestAction(2))
            onActionDispatch(TestAction(1))
            collector.snapshot().actions.bufferMaxOccupancy shouldBe 2
        }
    }

    "Action.bufferMaxOccupancy safe on empty removals" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onActionDispatch(TestAction(1))
            collector.snapshot().actions.bufferMaxOccupancy shouldBe 1
        }
    }

    "Action.bufferMaxOccupancy is 0 with no traffic" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().actions.bufferMaxOccupancy shouldBe 0
        }
    }

    "Action.bufferOverflows increments on undelivered action" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            onUndeliveredAction(TestAction(1))
            collector.snapshot().actions.bufferOverflows shouldBe 1
        }
    }

    "Action.bufferOverflows counts multiple overflows" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            onUndeliveredAction(TestAction(1))
            onUndeliveredAction(TestAction(2))
            collector.snapshot().actions.bufferOverflows shouldBe 2
        }
    }

    "Action.bufferOverflows is 0 when none" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().actions.bufferOverflows shouldBe 0
        }
    }

    "Action.pluginOverheadAvg/Median sampled from onAction plugin durations" {
        testCollectorWithTime(emaAlpha = 0.5, childFactory = { _, ts ->
            var call = 0
            plugin {
                onAction { action ->
                    call++
                    ts.advanceBy(if (call <= 2) 10.milliseconds else 20.milliseconds)
                    action
                }
            }
        }) { collector, _, _ ->
            onStart()
            repeat(3) { onAction(TestAction(it)) }
            val snap = collector.snapshot().actions
            snap.pluginOverheadAvg shouldBe 15.milliseconds
            snap.pluginOverheadMedian shouldBe 10.milliseconds
        }
    }

    "Action.pluginOverheadAvg/Median are 0 when no samples" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            val snap = collector.snapshot().actions
            snap.pluginOverheadAvg shouldBe ZERO
            snap.pluginOverheadMedian shouldBe ZERO
        }
    }

    "Action.pluginOverheadAvg/Median stable for identical durations" {
        testCollectorWithTime(emaAlpha = 0.5, childFactory = { _, ts ->
            plugin {
                onAction { action ->
                    ts.advanceBy(10.milliseconds)
                    action
                }
            }
        }) { collector, _, _ ->
            onStart()
            repeat(2) { onAction(TestAction(it)) }
            val snap = collector.snapshot().actions
            snap.pluginOverheadAvg shouldBe 10.milliseconds
            snap.pluginOverheadMedian shouldBe 10.milliseconds
        }
    }
})
