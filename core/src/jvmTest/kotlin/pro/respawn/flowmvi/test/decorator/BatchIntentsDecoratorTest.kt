package pro.respawn.flowmvi.test.decorator

import app.cash.turbine.test
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.api.DelicateStoreApi
import pro.respawn.flowmvi.decorator.decorates
import pro.respawn.flowmvi.decorators.BatchQueue
import pro.respawn.flowmvi.decorators.BatchingMode
import pro.respawn.flowmvi.decorators.batchIntentsDecorator
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.plugins.compositePlugin
import pro.respawn.flowmvi.plugins.reducePlugin
import pro.respawn.flowmvi.plugins.timeTravelPlugin
import pro.respawn.flowmvi.test.plugin.test
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.advanceBy
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.testTimeTravel
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalFlowMVIAPI::class, DelicateStoreApi::class)
class BatchIntentsDecoratorTest : FreeSpec({
    configure()

    "given amount batching mode" - {
        val timeTravel = testTimeTravel()
        val queue = BatchQueue<TestIntent>()
        val decorator = batchIntentsDecorator<TestState, TestIntent, TestAction>(
            mode = BatchingMode.Amount(size = 1),
            queue = queue
        )
        val plugin = compositePlugin(
            timeTravelPlugin(timeTravel),
            reducePlugin { with(it) { invoke() } }
        )

        "when intent count reaches the configured size" - {
            "then queued intents flush immediately" {
                (decorator decorates plugin).test(TestState.Some, timeTravel) {
                    onStart()
                    val latch = CompletableDeferred<Unit>()

                    onIntent(TestIntent { latch.complete(Unit) })

                    latch.await()

                    queue.queue.value.shouldBeEmpty()
                    timeTravel.intents.size shouldBe 1

                    onStop(null)
                }
            }
        }

    }

    "given time-based batching mode" - {
        val batchInterval = 100.milliseconds
        val timeTravel = testTimeTravel()
        val queue = BatchQueue<TestIntent>()
        val decorator = batchIntentsDecorator<TestState, TestIntent, TestAction>(
            mode = BatchingMode.Time(duration = batchInterval),
            queue = queue
        )
        val plugin = compositePlugin(
            timeTravelPlugin(timeTravel),
            reducePlugin { with(it) { invoke() } }
        )

        "when delay elapses after receiving intents" - {
            "then queued intents flush after the interval" {
                (decorator decorates plugin).test(TestState.Some, timeTravel) {
                    onStart()
                    val latch = CompletableDeferred<Unit>()
                    val first = TestIntent { }
                    val second = TestIntent { latch.complete(Unit) }

                    onIntent(first)
                    onIntent(second)

                    queue.queue.value shouldHaveSize 2
                    timeTravel.intents.size shouldBe 0

                    advanceBy(batchInterval)

                    latch.await()

                    timeTravel.intents.size shouldBe 2
                    queue.queue.value.shouldBeEmpty()

                    onStop(null)
                }
            }
        }

    }
})
