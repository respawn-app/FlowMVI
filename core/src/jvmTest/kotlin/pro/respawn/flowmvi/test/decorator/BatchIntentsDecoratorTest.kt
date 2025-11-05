package pro.respawn.flowmvi.test.decorator

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.decorator.decorates
import pro.respawn.flowmvi.decorators.BatchQueue
import pro.respawn.flowmvi.decorators.BatchingMode
import pro.respawn.flowmvi.decorators.batchIntentsDecorator
import pro.respawn.flowmvi.test.plugin.test
import pro.respawn.flowmvi.plugins.timeTravelPlugin
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.advanceBy
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.testTimeTravel
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalFlowMVIAPI::class)
class BatchIntentsDecoratorTest : FreeSpec({
    configure()

    "given amount batching mode" - {
        val timeTravel = testTimeTravel()
        val queue = BatchQueue<TestIntent>()
        val decorator = batchIntentsDecorator<TestState, TestIntent, TestAction>(
            mode = BatchingMode.Amount(size = 1),
            queue = queue
        )
        val plugin = timeTravelPlugin(timeTravel)

        "when intent count reaches the configured size" {
            (decorator decorates plugin).test(TestState.Some, timeTravel) {
                onStart()
                val intent = TestIntent { }

                onIntent(intent)

                queue.queue.value.shouldBeEmpty()
                timeTravel.intents.size shouldBe 1

                onStop(null)
            }
        }
    }

    "given time-based batching mode" - {
        val batchInterval = 10.milliseconds
        val timeTravel = testTimeTravel()
        val queue = BatchQueue<TestIntent>()
        val decorator = batchIntentsDecorator<TestState, TestIntent, TestAction>(
            mode = BatchingMode.Time(duration = batchInterval),
            queue = queue
        )
        val plugin = timeTravelPlugin(timeTravel)

        "when delay elapses after receiving intents" {
            (decorator decorates plugin).test(TestState.Some, timeTravel) {
                onStart()
                val first = TestIntent { }
                val second = TestIntent { }

                onIntent(first)
                onIntent(second)

                advanceBy(batchInterval)

                queue.queue.value.shouldBeEmpty()
                timeTravel.intents.size shouldBe 2

                onStop(null)
            }
        }
    }
})
