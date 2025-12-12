package pro.respawn.flowmvi.metrics

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.dsl.plugin
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds

class SubscriptionMetricsCollectorTest : FreeSpec({

    configure()

    "Subscriptions.events increments on subscribe and unsubscribe" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onSubscribe(1)
            onUnsubscribe(0)
            collector.snapshot().subscriptions.events shouldBe 2
        }
    }

    "Subscriptions.events counts multiple rapid changes" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onSubscribe(1)
            onSubscribe(2)
            onUnsubscribe(1)
            onUnsubscribe(0)
            collector.snapshot().subscriptions.events shouldBe 4
        }
    }

    "Subscriptions.events is 0 when no subscription changes" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().subscriptions.events shouldBe 0
        }
    }

    "Subscriptions.current tracks active after subscribe/unsubscribe" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onSubscribe(2)
            onUnsubscribe(1)
            collector.snapshot().subscriptions.current shouldBe 1
        }
    }

    "Subscriptions.current does not go below 0 on degenerate unsubscribe" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onUnsubscribe(-1)
            collector.snapshot().subscriptions.current shouldBe 0
        }
    }

    "Subscriptions.current is 0 when never subscribed" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().subscriptions.current shouldBe 0
        }
    }

    "Subscriptions.peak tracks highest concurrent count" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onSubscribe(1)
            onSubscribe(3)
            onUnsubscribe(1)
            collector.snapshot().subscriptions.peak shouldBe 3
        }
    }

    "Subscriptions.peak unchanged after drops" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onSubscribe(2)
            onUnsubscribe(0)
            collector.snapshot().subscriptions.peak shouldBe 2
        }
    }

    "Subscriptions.peak is 0 when no subscribers" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().subscriptions.peak shouldBe 0
        }
    }

    "Subscriptions.lifetimeAvg records subscribe to unsubscribe duration" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            onSubscribe(1)
            ts.advanceBy(100.milliseconds)
            onUnsubscribe(0)
            collector.snapshot().subscriptions.lifetimeAvg shouldBe 100.milliseconds
        }
    }

    "Subscriptions.lifetimeAvg is 0 when no unsubscribes" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onSubscribe(1)
            collector.snapshot().subscriptions.lifetimeAvg shouldBe ZERO
        }
    }

    "Subscriptions.lifetimeAvg is 0 for zero elapsed time" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onSubscribe(1)
            onUnsubscribe(0)
            collector.snapshot().subscriptions.lifetimeAvg shouldBe ZERO
        }
    }

    "Subscriptions.lifetimeMedian returns median lifetime" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            onSubscribe(1)
            ts.advanceBy(10.milliseconds)
            onUnsubscribe(0)
            onSubscribe(1)
            ts.advanceBy(20.milliseconds)
            onUnsubscribe(0)
            onSubscribe(1)
            ts.advanceBy(30.milliseconds)
            onUnsubscribe(0)
            collector.snapshot().subscriptions.lifetimeMedian shouldBe 20.milliseconds
        }
    }

    "Subscriptions.lifetimeMedian for single lifetime equals that value" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            onSubscribe(1)
            ts.advanceBy(15.milliseconds)
            onUnsubscribe(0)
            collector.snapshot().subscriptions.lifetimeMedian shouldBe 15.milliseconds
        }
    }

    "Subscriptions.lifetimeMedian is 0 when none" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().subscriptions.lifetimeMedian shouldBe ZERO
        }
    }

    "Subscriptions.subscribersAvg time-weighted average with changes" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, ts ->
            onStart()
            onSubscribe(2)
            ts.advanceBy(10.milliseconds)
            onSubscribe(1)
            ts.advanceBy(10.milliseconds)
            onUnsubscribe(0)
            collector.snapshot().subscriptions.subscribersAvg shouldBe (1.5 plusOrMinus 1e-9)
        }
    }

    "Subscriptions.subscribersAvg equals current when no elapsed time" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onSubscribe(3)
            collector.snapshot().subscriptions.subscribersAvg shouldBe 3.0
        }
    }

    "Subscriptions.subscribersAvg equals current when no samples" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().subscriptions.subscribersAvg shouldBe 0.0
        }
    }

    "Subscriptions.subscribersMedian returns median sampled count" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onSubscribe(1)
            onSubscribe(2)
            onUnsubscribe(0)
            collector.snapshot().subscriptions.subscribersMedian shouldBe 1.0
        }
    }

    "Subscriptions.subscribersMedian for single sample equals that count" {
        testCollectorWithTime(childFactory = { _, _ -> plugin { } }) { collector, _, _ ->
            onStart()
            onSubscribe(2)
            collector.snapshot().subscriptions.subscribersMedian shouldBe 2.0
        }
    }

    "Subscriptions.subscribersMedian is 0 when no samples" {
        testCollectorWithTime { collector, _, _ ->
            onStart()
            collector.snapshot().subscriptions.subscribersMedian shouldBe 0.0
        }
    }
})
