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
                queue.flush()

                queue.queue.value.shouldBeEmpty()
            }
        }
    }
})
