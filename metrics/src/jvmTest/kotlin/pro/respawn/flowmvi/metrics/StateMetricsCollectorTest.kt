package pro.respawn.flowmvi.metrics

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.dsl.plugin
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class StateMetricsCollectorTest : FreeSpec({

    configure()

    "State.transitions increments on onState call" {
        testCollectorWithTime(childFactory = { _, _ ->
            plugin { onState { _, new -> new } }
        }) { collector, _, _ ->
            onStart()
            onState(TestState(0), TestState(1))
            collector.snapshot().state.transitions shouldBe 1
        }
    }

    "State.transitions increments even when vetoed" {
        testCollectorWithTime(childFactory = { _, _ ->
            plugin { onState { _, _ -> null } }
        }) { collector, _, _ ->
            onStart()
            onState(TestState(0), TestState(1))
            val snap = collector.snapshot().state
            snap.transitions shouldBe 1
            snap.transitionsVetoed shouldBe 1
        }
    }

    "State.transitions is 0 when no updates" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().state.transitions shouldBe 0
        }
    }

    "State.transitionsVetoed increments on null or same-state result" {
        testCollectorWithTime(childFactory = { _, _ ->
            plugin { onState { old, _ -> old } }
        }) { collector, _, _ ->
            onStart()
            onState(TestState(0), TestState(1))
            collector.snapshot().state.transitionsVetoed shouldBe 1
        }
    }

    "State.transitionsVetoed stays 0 on non-veto transition" {
        testCollectorWithTime(childFactory = { _, _ ->
            plugin { onState { _, new -> new } }
        }) { collector, _, _ ->
            onStart()
            onState(TestState(0), TestState(1))
            collector.snapshot().state.transitionsVetoed shouldBe 0
        }
    }

    "State.transitionsVetoed is 0 with no events" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().state.transitionsVetoed shouldBe 0
        }
    }

    "State.updateAvg EMA over two reducer durations is ~15ms" {
        testCollectorWithTime(emaAlpha = 0.5, childFactory = { _, ts ->
            var call = 0
            plugin {
                onState { old, new ->
                    call++
                    ts.advanceBy(if (call == 1) 10.milliseconds else 20.milliseconds)
                    new
                }
            }
        }) { collector, _, _ ->
            onStart()
            onState(TestState(0), TestState(1))
            onState(TestState(1), TestState(2))
            collector.snapshot().state.updateAvg shouldBe 15.milliseconds
        }
    }

    "State.updateAvg is 0 when no states processed" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().state.updateAvg shouldBe ZERO
        }
    }

    "State.updateAvg stable for identical durations" {
        testCollectorWithTime(emaAlpha = 0.5, childFactory = { _, ts ->
            plugin { onState { _, new ->
                ts.advanceBy(10.milliseconds)
                new
            }
            }
        }) { collector, _, _ ->
            onStart()
            onState(TestState(0), TestState(1))
            onState(TestState(1), TestState(2))
            collector.snapshot().state.updateAvg shouldBe 10.milliseconds
        }
    }

    "State.update quantiles return expected P50/P90/P95/P99" {
        testCollectorWithTime(childFactory = { _, ts ->
            var d = 0
            plugin {
                onState { _, new ->
                    d += 10
                    ts.advanceBy(d.milliseconds)
                    new
                }
            }
        }) { collector, _, _ ->
            onStart()
            repeat(5) { i ->
                onState(TestState(i), TestState(i + 1))
            }
            val snap = collector.snapshot().state
            snap.updateP50 shouldBe 30.milliseconds
            snap.updateP90 shouldBe 50.milliseconds
            snap.updateP95 shouldBe 50.milliseconds
            snap.updateP99 shouldBe 50.milliseconds
        }
    }

    "State.update quantiles for single sample equal that sample" {
        testCollectorWithTime(childFactory = { _, ts ->
            plugin { onState { _, new ->
                ts.advanceBy(10.milliseconds)
                new
            }
            }
        }) { collector, _, _ ->
            onStart()
            onState(TestState(0), TestState(1))
            val snap = collector.snapshot().state
            snap.updateP50 shouldBe 10.milliseconds
            snap.updateP90 shouldBe 10.milliseconds
            snap.updateP95 shouldBe 10.milliseconds
            snap.updateP99 shouldBe 10.milliseconds
        }
    }

    "State.update quantiles are 0 when no samples" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            val snap = collector.snapshot().state
            snap.updateP50 shouldBe ZERO
            snap.updateP90 shouldBe ZERO
            snap.updateP95 shouldBe ZERO
            snap.updateP99 shouldBe ZERO
        }
    }

    "State.opsPerSecond reflects updates within window" {
        testCollectorWithTime(windowSeconds = 2, childFactory = { _, ts ->
            plugin { onState { _, new ->
                ts.advanceBy(1.milliseconds)
                new
            }
            }
        }) { collector, _, _ ->
            onStart()
            repeat(2) { i -> onState(TestState(i), TestState(i + 1)) }
            collector.snapshot().state.opsPerSecond shouldBe (1.0 plusOrMinus 1e-9)
        }
    }

    "State.opsPerSecond is 0 when no updates" {
        testCollectorWithTime(windowSeconds = 2) { collector, _, _ ->
            onStart()
            collector.snapshot().state.opsPerSecond shouldBe 0.0
        }
    }

    "State.opsPerSecond drops updates outside window" {
        testCollectorWithTime(windowSeconds = 2, childFactory = { _, ts ->
            plugin { onState { _, new ->
                ts.advanceBy(1.milliseconds)
                new
            }
            }
        }) { collector, clock, _ ->
            onStart()
            repeat(2) { i -> onState(TestState(i), TestState(i + 1)) }
            clock.advanceBy(3.seconds)
            collector.snapshot().state.opsPerSecond shouldBe 0.0
        }
    }
})
