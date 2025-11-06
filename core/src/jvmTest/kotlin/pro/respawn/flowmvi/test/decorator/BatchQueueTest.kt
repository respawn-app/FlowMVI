package pro.respawn.flowmvi.test.decorator

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.decorators.BatchQueue
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.configure

class BatchQueueTest : FreeSpec({
    configure()

    "given batch queue" - {
        "when intents pushed" - {
            val first = TestIntent { }
            val second = TestIntent { }

            "then flush returns all intents" {
                val queue = BatchQueue<TestIntent>()
                queue.push(first)
                queue.push(second)

                queue.flush() shouldBe listOf(first, second)
            }

            "then queue is emptied after flush" {
                val queue = BatchQueue<TestIntent>()
                queue.push(first)
                val _ = queue.flush()

                queue.queue.value.shouldBeEmpty()
            }

            "then pushing below threshold keeps items" {
                val queue = BatchQueue<TestIntent>()
                queue.pushAndFlushIfReached(first, size = 2) shouldBe emptyList()
                queue.queue.value shouldBe listOf(first)
            }

            "then pushing reaching threshold flushes" {
                val queue = BatchQueue<TestIntent>()
                queue.push(first)
                queue.pushAndFlushIfReached(second, size = 2) shouldBe listOf(first, second)
                queue.queue.value.shouldBeEmpty()
            }
        }
    }
})
