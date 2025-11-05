package pro.respawn.flowmvi.test.decorator

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.decorator.decorates
import pro.respawn.flowmvi.decorators.RetryStrategy
import pro.respawn.flowmvi.decorators.retryActionsDecorator
import pro.respawn.flowmvi.decorators.retryIntentsDecorator
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.test.plugin.test
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.testTimeTravel

@OptIn(ExperimentalFlowMVIAPI::class)
class RetryDecoratorTest : FreeSpec({
    configure()

    "given retry intents decorator" - {
        val intent = TestIntent { }
        "when child succeeds after retries" - {
            "then retries exhaust before success" {
                var attempts = 0
                val child = plugin<TestState, TestIntent, TestAction> {
                    onIntent {
                        attempts += 1
                        if (attempts < 3) throw IllegalStateException("fail $attempts")
                        null
                    }
                }
                val decorator = retryIntentsDecorator<TestState, TestIntent, TestAction>(
                    strategy = RetryStrategy.Immediate(retries = 2)
                )

                (decorator decorates child).test(TestState.Some) {
                    onStart()
                    onIntent(intent) shouldBe null
                }
                attempts shouldBe 3
            }
        }
        "when selector rejects retry" - {
            "then exception surfaces immediately" {
                var attempts = 0
                val child = plugin<TestState, TestIntent, TestAction> {
                    onIntent {
                        attempts += 1
                        if (attempts == 1) throw IllegalStateException("retryable")
                        throw IllegalArgumentException("stop")
                    }
                }
                val decorator = retryIntentsDecorator<TestState, TestIntent, TestAction>(
                    strategy = RetryStrategy.Immediate(retries = 3),
                    selector = { _, e -> e is IllegalStateException }
                )

                shouldThrowExactly<IllegalArgumentException> {
                    (decorator decorates child).test(TestState.Some) {
                        onStart()
                        onIntent(intent)
                    }
                }
                attempts shouldBe 2
            }
        }
        "when attempts exceed configured retries" - {
            "then decorator rethrows last exception" {
                var attempts = 0
                val child = plugin<TestState, TestIntent, TestAction> {
                    onIntent {
                        attempts += 1
                        throw IllegalStateException("fail $attempts")
                    }
                }
                val decorator = retryIntentsDecorator<TestState, TestIntent, TestAction>(
                    strategy = RetryStrategy.Immediate(retries = 1)
                )

                shouldThrowExactly<IllegalStateException> {
                    (decorator decorates child).test(TestState.Some) {
                        onStart()
                        onIntent(intent)
                    }
                }
                attempts shouldBe 2
            }
        }
    }

    "given retry actions decorator" - {
        val action = TestAction.Some
        "when child succeeds after retries" - {
            "then retries exhaust before success" {
                var attempts = 0
                val child = plugin<TestState, TestIntent, TestAction> {
                    onAction {
                        attempts += 1
                        if (attempts < 2) throw IllegalStateException("fail $attempts")
                        action
                    }
                }
                val decorator = retryActionsDecorator<TestState, TestIntent, TestAction>(
                    strategy = RetryStrategy.Immediate(retries = 1)
                )

                (decorator decorates child).test(TestState.Some, testTimeTravel()) {
                    onStart()
                    onAction(action) shouldBe action
                }
                attempts shouldBe 2
            }
        }
    }
})
