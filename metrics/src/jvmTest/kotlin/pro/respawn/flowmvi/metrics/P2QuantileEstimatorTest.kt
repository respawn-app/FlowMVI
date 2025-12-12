package pro.respawn.flowmvi.metrics

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeNaN
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

class P2QuantileEstimatorTest : FreeSpec({

    "constructor validation fails for invalid probabilities" {
        shouldThrow<IllegalArgumentException> { P2QuantileEstimator() }
        shouldThrow<IllegalArgumentException> { P2QuantileEstimator(-0.1) }
        shouldThrow<IllegalArgumentException> { P2QuantileEstimator(0.0) }
        shouldThrow<IllegalArgumentException> { P2QuantileEstimator(1.0) }
        shouldThrow<IllegalArgumentException> { P2QuantileEstimator(0.2, 0.2) }
    }

    "constructor accepts unsorted probabilities" {
        shouldNotThrowAny { P2QuantileEstimator(0.9, 0.5) }
    }

    "bootstrap path uses sorted samples when fewer than markerCount" {
        val estimator = P2QuantileEstimator(0.5)

        listOf(3.0, 1.0, 2.0).forEach { estimator.add(it) }
        estimator.count shouldBe 3
        estimator.getQuantile(0.5) shouldBe 2.0
    }

    "exact bootstrap size initializes markers" {
        val estimator = P2QuantileEstimator(0.5)

        listOf(10.0, 20.0, 30.0, 40.0, 50.0).forEach { estimator.add(it) }
        estimator.count shouldBe 5
        estimator.getQuantile(0.5) shouldBe 30.0
    }

    "clear after bootstrap resets state" {
        val estimator = P2QuantileEstimator(0.5)

        repeat(5) { estimator.add(it.toDouble()) }
        estimator.clear()

        estimator.count shouldBe 0
        estimator.getQuantile(0.5).shouldBeNaN()
    }

    "monotonic samples yield accurate quantiles" {
        val estimator = P2QuantileEstimator(0.25, 0.5, 0.75)

        for (i in 1..100) estimator.add(i.toDouble())

        estimator.getQuantile(0.25) shouldBe (25.0 plusOrMinus 3.0)
        estimator.getQuantile(0.5) shouldBe (50.0 plusOrMinus 3.0)
        estimator.getQuantile(0.75) shouldBe (75.0 plusOrMinus 3.0)
    }

    "identical samples keep quantiles unchanged" {
        val estimator = P2QuantileEstimator(0.5)

        repeat(20) { estimator.add(42.0) }

        estimator.getQuantile(0.5) shouldBe 42.0
    }

    "alternating extremes keep markers bounded" {
        val estimator = P2QuantileEstimator(0.25, 0.5, 0.75)

        repeat(50) { i ->
            estimator.add(if (i % 2 == 0) 0.0 else 1000.0)
        }

        listOf(0.25, 0.5, 0.75).forEach { p ->
            val q = estimator.getQuantile(p)
            (q in 0.0..1000.0) shouldBe true
        }
    }

    "concurrent adds are safe and update count" {
        val estimator = P2QuantileEstimator(0.5)
        val totalAdds = 1_000

        coroutineScope {
            repeat(4) { worker ->
                launch(Dispatchers.Default) {
                    repeat(totalAdds / 4) { estimator.add(it + worker * 1000.0) }
                }
            }
        }

        estimator.count shouldBe totalAdds
        estimator.getQuantile(0.5).isFinite() shouldBe true
    }

    "getQuantile during concurrent adds does not throw" {
        val estimator = P2QuantileEstimator(0.5)

        coroutineScope {
            val writer = launch(Dispatchers.Default) {
                repeat(50) {
                    estimator.add(it.toDouble())
                    if (it == 25) estimator.clear()
                    yield()
                }
            }
            val reader = launch(Dispatchers.Default) {
                repeat(50) { estimator.getQuantile(0.5) }
            }
            writer.join()
            reader.join()
        }

        estimator.count shouldNotBe 0
    }

    "clear resets state and subsequent adds work" {
        val estimator = P2QuantileEstimator(0.5)

        repeat(10) { estimator.add(it.toDouble()) }
        estimator.clear()
        estimator.count shouldBe 0

        estimator.add(123.0)
        estimator.getQuantile(0.5) shouldBe 123.0
    }

    "getQuantile validation and edge cases" {
        val estimator = P2QuantileEstimator(0.5)

        estimator.getQuantile(0.5).shouldBeNaN()
        estimator.add(7.0)
        estimator.getQuantile(0.5) shouldBe 7.0
        repeat(10) { estimator.add(it.toDouble()) }

        shouldThrow<IllegalArgumentException> { estimator.getQuantile(0.9) }
    }
})
