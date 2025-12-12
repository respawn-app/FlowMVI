package pro.respawn.flowmvi.metrics

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeNaN
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class EmaAndTimeMarkQueueTest : FreeSpec({

    "ema accumulates with correct formula and baseline" {
        val ema = Ema(alpha = 0.2)

        ema.value.shouldBeNaN()
        ema.add(10.0)
        ema.value shouldBe 10.0
        ema.add(20.0)

        ema.value shouldBe (12.0 plusOrMinus 1e-9)
        ema.count shouldBe 2
    }

    "ema duration overload matches double path" {
        val ema = Ema(alpha = 0.5)

        ema += 100.milliseconds
        val durationValue = ema.value
        ema.reset()
        ema += 100.0

        ema.value shouldBe durationValue
        ema.count shouldBe 1
    }

    "ema reset clears value and supports multiple resets" {
        val ema = Ema(alpha = 0.3)

        ema.add(5.0)
        ema.reset()
        ema.value.shouldBeNaN()
        ema.count shouldBe 0

        ema.reset()
        ema.value.shouldBeNaN()
        ema.count shouldBe 0
    }

    "timeMarkQueue maintains FIFO semantics" {
        val queue = TimeMarkQueue()
        val m1 = TimeSource.Monotonic.markNow()
        val m2 = TimeSource.Monotonic.markNow()

        queue.addLast(m1)
        queue.addLast(m2)

        queue.removeFirstOrNull() shouldBe m1
        queue.removeFirstOrNull() shouldBe m2
        queue.removeFirstOrNull() shouldBe null
        queue.size shouldBe 0
    }

    "timeMarkQueue clear empties queue" {
        val queue = TimeMarkQueue()
        repeat(3) { queue.addLast(TimeSource.Monotonic.markNow()) }

        queue.clear()

        queue.size shouldBe 0
        queue.removeFirstOrNull() shouldBe null
    }

    "timeMarkQueue size tracks concurrent add/remove under lock" {
        val queue = TimeMarkQueue()

        coroutineScope {
            val producer = launch {
                repeat(100) {
                    queue.addLast(TimeSource.Monotonic.markNow())
                    yield()
                }
            }
            val consumer = launch {
                repeat(100) {
                    while (queue.removeFirstOrNull() == null) yield()
                }
            }
            producer.join()
            consumer.join()
        }

        queue.size shouldBe 0
    }
})
