package pro.respawn.flowmvi.test.decorator

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.decorator.decorates
import pro.respawn.flowmvi.decorators.conflateActionsDecorator
import pro.respawn.flowmvi.decorators.conflateIntentsDecorator
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.test.plugin.test
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.testTimeTravel

@OptIn(ExperimentalFlowMVIAPI::class)
class ConflateDecoratorTest : FreeSpec({
    configure()

    "given conflate intents decorator" - {
        "when distinct intents dispatched" - {
            "then all intents reach the child" {
                var calls = 0
                val child = plugin<TestState, TestIntent, TestAction> {
                    onIntent { intent ->
                        calls += 1
                        intent
                    }
                }
                val decorator = conflateIntentsDecorator<TestState, TestIntent, TestAction>()

                (decorator decorates child).test(TestState.Some, testTimeTravel()) {
                    onStart()
                    onIntent(TestIntent { })
                    onIntent(TestIntent { })

                    calls shouldBe 2
                }
            }
        }
        "when equal intents dispatched back to back" - {
            "then duplicates are dropped" {
                var calls = 0
                val child = plugin<TestState, TestIntent, TestAction> {
                    onIntent { intent ->
                        calls += 1
                        intent
                    }
                }
                val decorator = conflateIntentsDecorator<TestState, TestIntent, TestAction>()

                (decorator decorates child).test(TestState.Some, testTimeTravel()) {
                    onStart()
                    val repeated = TestIntent { }

                    repeat(3) { onIntent(repeated) }

                    calls shouldBe 1
                }
            }
        }
    }

    "given conflate actions decorator" - {
        "when equal actions dispatched" - {
            "then duplicates are dropped" {
                var calls = 0
                val child = plugin<TestState, TestIntent, TestAction> {
                    onAction { action ->
                        calls += 1
                        action
                    }
                }
                val decorator = conflateActionsDecorator<TestState, TestIntent, TestAction>()

                (decorator decorates child).test(TestState.Some, testTimeTravel()) {
                    onStart()
                    repeat(3) { onAction(TestAction.Some) }

                    calls shouldBe 1
                }
            }
        }
        "when distinct actions dispatched" - {
            "then all actions reach the child" {
                var calls = 0
                val child = plugin<TestState, TestIntent, TestAction> {
                    onAction { action ->
                        calls += 1
                        action
                    }
                }
                val decorator = conflateActionsDecorator<TestState, TestIntent, TestAction>()

                (decorator decorates child).test(TestState.Some, testTimeTravel()) {
                    onStart()
                    onAction(TestAction.Some)
                    onAction(TestAction.SomeData(1))

                    calls shouldBe 2
                }
            }
        }
    }
})
