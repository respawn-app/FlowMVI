package pro.respawn.flowmvi.test.decorator

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.decorators.RetryStrategy
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

// todo - test the rest of the decorator

private fun testDelays(s: RetryStrategy, delays: List<Duration>) {
    repeat(s.retries) { i ->
        // attempts start at 1 from the client
        val attempt = i + 1
        s.shouldRetry(attempt) shouldBe true
        s.delay(attempt).shouldBe(delays[i], "delay for $attempt should be ${delays[i]}")
    }
    // 4 th retry not allowed
    s.shouldRetry(s.retries + 1) shouldBe false
}

class RetryStrategyTest : FreeSpec({

    val delay = 1.seconds
    val exp = 2.0

    "Given 0 retries" - {
        val retries = 0
        "then constructor throws" {
            shouldThrow<IllegalArgumentException> {
                RetryStrategy.ExponentialDelay(retries, 1.seconds)
            }
        }
    }

    "Given 3 retries, delay of $delay and exp of ${exp.toInt()}" - {
        val retries = 3
        "and delayInitially = false" - {
            val delayInitially = false
            "then the delays are 0, 1, 2" {
                val s = RetryStrategy.ExponentialDelay(retries, delay, exp, delayInitially)
                testDelays(s, listOf(0.seconds, 1.seconds, 2.seconds))
            }
        }
        "and delayInitially = true" - {
            val delayInitially = true
            "then the delays are 1, 2, 4" {
                val s = RetryStrategy.ExponentialDelay(retries, delay, exp, delayInitially)
                testDelays(s, listOf(1.seconds, 2.seconds, 4.seconds))
            }
        }
    }
})
