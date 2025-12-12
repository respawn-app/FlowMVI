package pro.respawn.flowmvi.metrics

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.yield
import pro.respawn.flowmvi.dsl.plugin
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class IntentMetricsCollectorTest : FreeSpec({

    configure()

    "Intent.total increments per enqueue" {
        testCollectorWithTime(childFactory = { _, _ ->
            plugin {
                onIntentEnqueue { it }
            }
        }) { collector, _, _ ->
            onStart()
            onIntentEnqueue(TestIntent(1))
            onIntentEnqueue(TestIntent(2))
            onIntentEnqueue(TestIntent(3))
            collector.snapshot().intents.total shouldBe 3
        }
    }

    "Intent.total increments even when dropped at enqueue" {
        testCollectorWithTime(childFactory = { _, _ ->
            plugin {
                onIntentEnqueue { null }
            }
        }) { collector, _, _ ->
            onStart()
            onIntentEnqueue(TestIntent(1))
            onIntentEnqueue(TestIntent(2))
            collector.snapshot().intents.total shouldBe 2
        }
    }

    "Intent.total is 0 when no enqueues" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().intents.total shouldBe 0
        }
    }

    "Intent.processed increments on processed intent" {
        testCollectorWithTime(childFactory = { _, ts ->
            plugin {
                onIntent { intent ->
                    ts.advanceBy(10.milliseconds)
                    intent
                }
            }
        }) { collector, _, _ ->
            onStart()
            onIntentEnqueue(TestIntent(1))
            onIntent(TestIntent(1))
            collector.snapshot().intents.processed shouldBe 1
        }
    }

    "Intent.processed increments even when onIntent returns null" {
        testCollectorWithTime(childFactory = { _, _ ->
            plugin {
                onIntent { null }
            }
        }) { collector, _, _ ->
            onStart()
            onIntentEnqueue(TestIntent(1))
            onIntent(TestIntent(1))
            val snap = collector.snapshot().intents
            snap.processed shouldBe 1
            snap.dropped shouldBe 1
        }
    }

    "Intent.processed is 0 when no intents processed" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().intents.processed shouldBe 0
        }
    }

    "Intent.dropped increments on enqueue veto" {
        testCollectorWithTime(childFactory = { _, _ ->
            plugin {
                onIntentEnqueue { null }
            }
        }) { collector, _, _ ->
            onStart()
            onIntentEnqueue(TestIntent(1))
            collector.snapshot().intents.dropped shouldBe 1
        }
    }

    "Intent.dropped increments when onIntent returns null" {
        testCollectorWithTime(childFactory = { _, _ ->
            plugin {
                onIntent { null }
            }
        }) { collector, _, _ ->
            onStart()
            onIntentEnqueue(TestIntent(1))
            onIntent(TestIntent(1))
            collector.snapshot().intents.dropped shouldBe 1
        }
    }

    "Intent.dropped is 0 when none dropped" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().intents.dropped shouldBe 0
        }
    }

    "Intent.undelivered increments on undelivered intent" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            onUndeliveredIntent(TestIntent(1))
            collector.snapshot().intents.undelivered shouldBe 1
        }
    }

    "Intent.undelivered counts multiple undelivered intents" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            onUndeliveredIntent(TestIntent(1))
            onUndeliveredIntent(TestIntent(2))
            collector.snapshot().intents.undelivered shouldBe 2
        }
    }

    "Intent.undelivered is 0 when none undelivered" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().intents.undelivered shouldBe 0
        }
    }

    "Intent.opsPerSecond reflects intents within window" {
        testCollectorWithTime(windowSeconds = 3, childFactory = { _, ts ->
            plugin {
                onIntent { intent ->
                    ts.advanceBy(1.milliseconds)
                    intent
                }
            }
        }) { collector, _, _ ->
            onStart()
            repeat(3) {
                onIntentEnqueue(TestIntent(it))
                onIntent(TestIntent(it))
            }
            collector.snapshot().intents.opsPerSecond shouldBe (1.0 plusOrMinus 1e-9)
        }
    }

    "Intent.opsPerSecond is 0 when no intents" {
        testCollectorWithTime(windowSeconds = 3) { collector, _, _ ->
            onStart()
            collector.snapshot().intents.opsPerSecond shouldBe 0.0
        }
    }

    "Intent.opsPerSecond drops samples outside window" {
        testCollectorWithTime(windowSeconds = 3, childFactory = { _, ts ->
            plugin { onIntent {
                ts.advanceBy(1.milliseconds);
                it
            }
            }
        }) { collector, clock, _ ->
            onStart()
            repeat(3) {
                onIntentEnqueue(TestIntent(it))
                onIntent(TestIntent(it))
            }
            clock.advanceBy(4.seconds)
            collector.snapshot().intents.opsPerSecond shouldBe 0.0
        }
    }

    "Intent.durationAvg EMA over two intents is ~15ms" {
        testCollectorWithTime(emaAlpha = 0.5, childFactory = { _, ts ->
            var call = 0
            plugin {
                onIntent { intent ->
                    call++
                    ts.advanceBy(if (call == 1) 10.milliseconds else 20.milliseconds)
                    intent
                }
            }
        }) { collector, _, _ ->
            onStart()
            onIntentEnqueue(TestIntent(1));
            onIntent(TestIntent(1))
            onIntentEnqueue(TestIntent(2));
            onIntent(TestIntent(2))
            collector.snapshot().intents.durationAvg shouldBe 15.milliseconds
        }
    }

    "Intent.durationAvg is 0 when no intents" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().intents.durationAvg shouldBe ZERO
        }
    }

    "Intent.durationAvg unchanged for identical durations" {
        testCollectorWithTime(emaAlpha = 0.5, childFactory = { _, ts ->
            plugin { onIntent {
                ts.advanceBy(10.milliseconds);
                it
            }
            }
        }) { collector, _, _ ->
            onStart()
            repeat(2) {
                onIntentEnqueue(TestIntent(it));
                onIntent(TestIntent(it))
            }
            collector.snapshot().intents.durationAvg shouldBe 10.milliseconds
        }
    }

    "Intent.duration quantiles return expected P50/P90/P95/P99" {
        testCollectorWithTime(childFactory = { _, ts ->
            var d = 0
            plugin {
                onIntent {
                    d += 10
                    ts.advanceBy(d.milliseconds)
                    it
                }
            }
        }) { collector, _, _ ->
            onStart()
            repeat(5) {
                onIntentEnqueue(TestIntent(it));
                onIntent(TestIntent(it))
            }
            val snap = collector.snapshot().intents
            snap.durationP50 shouldBe 30.milliseconds
            snap.durationP90 shouldBe 50.milliseconds
            snap.durationP95 shouldBe 50.milliseconds
            snap.durationP99 shouldBe 50.milliseconds
        }
    }

    "Intent.duration quantiles for single sample equal that sample" {
        testCollectorWithTime(childFactory = { _, ts ->
            plugin { onIntent {
                ts.advanceBy(10.milliseconds);
                it
            }
            }
        }) { collector, _, _ ->
            onStart()
            onIntentEnqueue(TestIntent(1));
            onIntent(TestIntent(1))
            val snap = collector.snapshot().intents
            snap.durationP50 shouldBe 10.milliseconds
            snap.durationP90 shouldBe 10.milliseconds
            snap.durationP95 shouldBe 10.milliseconds
            snap.durationP99 shouldBe 10.milliseconds
        }
    }

    "Intent.duration quantiles are 0 when no samples" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            val snap = collector.snapshot().intents
            snap.durationP50 shouldBe ZERO
            snap.durationP90 shouldBe ZERO
            snap.durationP95 shouldBe ZERO
            snap.durationP99 shouldBe ZERO
        }
    }

    "Intent.queueTimeAvg records enqueue to process delay" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            onIntentEnqueue(TestIntent(1))
            ts.advanceBy(5.milliseconds)
            onIntent(TestIntent(1))
            collector.snapshot().intents.queueTimeAvg shouldBe 5.milliseconds
        }
    }

    "Intent.queueTimeAvg stays 0 when processing with empty queue" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onIntent(TestIntent(1))
            collector.snapshot().intents.queueTimeAvg shouldBe ZERO
        }
    }

    "Intent.queueTimeAvg EMA advances with multiple samples" {
        testCollectorWithTime(emaAlpha = 0.5, childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            onIntentEnqueue(TestIntent(1))
            ts.advanceBy(5.milliseconds)
            onIntent(TestIntent(1))

            onIntentEnqueue(TestIntent(2))
            ts.advanceBy(15.milliseconds)
            onIntent(TestIntent(2))

            collector.snapshot().intents.queueTimeAvg shouldBe 10.milliseconds
        }
    }

    "Intent.inFlightMax reaches 2 for overlapping intents" {
        val started = AtomicInteger(0)
        val bothStarted = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        testCollectorWithTime(childFactory = { _, ts ->
            plugin {
                onIntent { intent ->
                    ts.advanceBy(1.milliseconds)
                    if (started.incrementAndGet() == 2) bothStarted.complete(Unit)
                    release.await()
                    intent
                }
            }
        }) { collector, _, _ ->
            onStart()
            coroutineScope {
                val job1 = async { onIntent(TestIntent(1)) }
                val job2 = async { onIntent(TestIntent(2)) }
                bothStarted.await()
                collector.snapshot().intents.inFlightMax shouldBe 2
                release.complete(Unit)
                job1.await()
                job2.await()
            }
        }
    }

    "Intent.inFlightMax decrements on exception so max stable" {
        val started = AtomicInteger(0)
        val bothStarted = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        testCollectorWithTime(childFactory = { _, ts ->
            plugin {
                onIntent { intent ->
                    ts.advanceBy(1.milliseconds)
                    if (started.incrementAndGet() == 2) bothStarted.complete(Unit)
                    release.await()
                    if (intent.id == 1) throw IllegalStateException("boom")
                    intent
                }
            }
        }) { collector, _, _ ->
            onStart()
            supervisorScope {
                val job1 = async { onIntent(TestIntent(1)) }
                val job2 = async { onIntent(TestIntent(2)) }
                bothStarted.await()
                release.complete(Unit)
                runCatching { job1.await() }
                job2.await()
            }
            collector.snapshot().intents.inFlightMax shouldBe 2
        }
    }

    "Intent.inFlightMax is 0 when no intents" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().intents.inFlightMax shouldBe 0
        }
    }

    "Intent.interArrivalAvg for 50ms spacing is 50ms" {
        testCollectorWithTime(emaAlpha = 0.5, childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            onIntentEnqueue(TestIntent(1))
            ts.advanceBy(50.milliseconds)
            onIntentEnqueue(TestIntent(2))
            collector.snapshot().intents.interArrivalAvg shouldBe 50.milliseconds
        }
    }

    "Intent.interArrivalAvg ignores first intent" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onIntentEnqueue(TestIntent(1))
            collector.snapshot().intents.interArrivalAvg shouldBe ZERO
        }
    }

    "Intent.interArrivalAvg EMA updates after burst and pause" {
        testCollectorWithTime(emaAlpha = 0.5, childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            onIntentEnqueue(TestIntent(1))
            ts.advanceBy(10.milliseconds)
            onIntentEnqueue(TestIntent(2))
            ts.advanceBy(10.milliseconds)
            onIntentEnqueue(TestIntent(3))
            ts.advanceBy(100.milliseconds)
            onIntentEnqueue(TestIntent(4))
            collector.snapshot().intents.interArrivalAvg shouldBe 55.milliseconds
        }
    }

    "Intent.interArrivalMedian returns median interval" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            onIntentEnqueue(TestIntent(1))
            ts.advanceBy(20.milliseconds)
            onIntentEnqueue(TestIntent(2))
            ts.advanceBy(40.milliseconds)
            onIntentEnqueue(TestIntent(3))
            ts.advanceBy(60.milliseconds)
            onIntentEnqueue(TestIntent(4))
            collector.snapshot().intents.interArrivalMedian shouldBe 40.milliseconds
        }
    }

    "Intent.interArrivalMedian for single interval equals that interval" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            onIntentEnqueue(TestIntent(1))
            ts.advanceBy(25.milliseconds)
            onIntentEnqueue(TestIntent(2))
            collector.snapshot().intents.interArrivalMedian shouldBe 25.milliseconds
        }
    }

    "Intent.interArrivalMedian is 0 when no intervals" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            onIntentEnqueue(TestIntent(1))
            collector.snapshot().intents.interArrivalMedian shouldBe ZERO
        }
    }

    "Intent.burstMax is 3 for three enqueues in same second" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            repeat(3) { onIntentEnqueue(TestIntent(it)) }
            collector.snapshot().intents.burstMax shouldBe 3
        }
    }

    "Intent.burstMax does not increase across second boundary" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, clock, _ ->
            onStart()
            onIntentEnqueue(TestIntent(1))
            onIntentEnqueue(TestIntent(2))
            clock.advanceBy(2.seconds)
            onIntentEnqueue(TestIntent(3))
            collector.snapshot().intents.burstMax shouldBe 2
        }
    }

    "Intent.burstMax is 1 for a single intent" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onIntentEnqueue(TestIntent(1))
            collector.snapshot().intents.burstMax shouldBe 1
        }
    }

    "Intent.bufferMaxOccupancy tracks queue + inFlight" {
        val release = CompletableDeferred<Unit>()
        testCollectorWithTime(childFactory = { _, ts ->
            plugin {
                onIntent { intent ->
                    release.await()
                    ts.advanceBy(1.milliseconds)
                    intent
                }
            }
        }) { collector, _, _ ->
            onStart()
            coroutineScope {
                val job = async { onIntent(TestIntent(1)) }
                yield()
                onIntentEnqueue(TestIntent(2))
                onIntentEnqueue(TestIntent(3))
                collector.snapshot().intents.bufferMaxOccupancy shouldBe 3
                release.complete(Unit)
                job.await()
            }
        }
    }

    "Intent.bufferMaxOccupancy does not decrease on empty dispatch" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onIntentEnqueue(TestIntent(1))
            onIntentEnqueue(TestIntent(2))
            onIntent(TestIntent(1))
            val before = collector.snapshot().intents.bufferMaxOccupancy
            onIntent(TestIntent(999))
            collector.snapshot().intents.bufferMaxOccupancy shouldBe before
        }
    }

    "Intent.bufferMaxOccupancy is 0 with no traffic" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().intents.bufferMaxOccupancy shouldBe 0
        }
    }

    "Intent.bufferOverflows increments on undelivered intent" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            onUndeliveredIntent(TestIntent(1))
            collector.snapshot().intents.bufferOverflows shouldBe 1
        }
    }

    "Intent.bufferOverflows counts multiple overflows" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            onUndeliveredIntent(TestIntent(1))
            onUndeliveredIntent(TestIntent(2))
            collector.snapshot().intents.bufferOverflows shouldBe 2
        }
    }

    "Intent.bufferOverflows is 0 when none" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().intents.bufferOverflows shouldBe 0
        }
    }

    "Intent.pluginOverheadAvg EMA updates with plugin durations" {
        testCollectorWithTime(emaAlpha = 0.5, childFactory = { _, ts ->
            var call = 0
            plugin {
                onIntent { intent ->
                    call++
                    ts.advanceBy(if (call == 1) 10.milliseconds else 20.milliseconds)
                    intent
                }
            }
        }) { collector, _, _ ->
            onStart()
            onIntentEnqueue(TestIntent(1));
            onIntent(TestIntent(1))
            onIntentEnqueue(TestIntent(2));
            onIntent(TestIntent(2))
            collector.snapshot().intents.pluginOverheadAvg shouldBe 15.milliseconds
        }
    }

    "Intent.pluginOverheadAvg is 0 with no plugin work" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().intents.pluginOverheadAvg shouldBe ZERO
        }
    }

    "Intent.pluginOverheadAvg stable for identical durations" {
        testCollectorWithTime(emaAlpha = 0.5, childFactory = { _, ts ->
            plugin { onIntent {
                ts.advanceBy(10.milliseconds);
                it
            }
            }
        }) { collector, _, _ ->
            onStart()
            repeat(2) {
                onIntentEnqueue(TestIntent(it));
                onIntent(TestIntent(it))
            }
            collector.snapshot().intents.pluginOverheadAvg shouldBe 10.milliseconds
        }
    }

    "Intent.pluginOverheadMedian matches samples" {
        testCollectorWithTime(childFactory = { _, ts ->
            var d = 0
            plugin {
                onIntent {
                    d += 5
                    ts.advanceBy(d.milliseconds)
                    it
                }
            }
        }) { collector, _, _ ->
            onStart()
            repeat(5) {
                onIntentEnqueue(TestIntent(it));
                onIntent(TestIntent(it))
            }
            collector.snapshot().intents.pluginOverheadMedian shouldBe 15.milliseconds
        }
    }

    "Intent.pluginOverheadMedian for single sample equals that value" {
        testCollectorWithTime(childFactory = { _, ts ->
            plugin { onIntent {
                ts.advanceBy(12.milliseconds);
                it
            }
            }
        }) { collector, _, _ ->
            onStart()
            onIntentEnqueue(TestIntent(1));
            onIntent(TestIntent(1))
            collector.snapshot().intents.pluginOverheadMedian shouldBe 12.milliseconds
        }
    }

    "Intent.pluginOverheadMedian is 0 when no samples" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().intents.pluginOverheadMedian shouldBe ZERO
        }
    }
})
