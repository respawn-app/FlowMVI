package pro.respawn.flowmvi.test.decorator

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.decorator.decorates
import pro.respawn.flowmvi.decorators.debounceIntentsDecorator
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.test.plugin.test
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.advanceBy
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.idle
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalFlowMVIAPI::class)
class DebounceIntentsDecoratorTest : FreeSpec({
    configure()

    "when intents arrive within the debounce window" - {
        val timeout = 100.milliseconds
        val received = mutableListOf<TestIntent>()
        val child = plugin<TestState, TestIntent, TestAction> {
            onIntent { intent ->
                received += intent
                intent
            }
        }
        val decorator = debounceIntentsDecorator<TestState, TestIntent, TestAction>(timeout = timeout)

        "then only the latest is delivered after the timeout" {
            (decorator decorates child).test(TestState.Some) {
                onStart()
                val first = TestIntent { }
                val second = TestIntent { }

                onIntent(first)
                advanceBy(timeout / 2)

                onIntent(second)

                received.shouldBeEmpty()

                advanceBy(timeout)

                received.shouldContainExactly(second)
            }
        }
    }

    "when debounce duration is non-positive" - {
        val received = mutableListOf<TestIntent>()
        val child = plugin<TestState, TestIntent, TestAction> {
            onIntent { intent ->
                received += intent
                intent
            }
        }
        val decorator = debounceIntentsDecorator<TestState, TestIntent, TestAction>(timeout = ZERO)

        "then intents bypass delay immediately" {
            (decorator decorates child).test(TestState.Some) {
                onStart()
                val intent = TestIntent { }

                onIntent(intent) shouldBe intent

                idle()
                received.shouldContainExactly(intent)
            }
        }
    }

    "when a later intent uses a shorter selector duration" - {
        val timeout = 100.milliseconds
        val received = mutableListOf<String>()
        var first: TestIntent? = null
        var second: TestIntent? = null
        val child = plugin<TestState, TestIntent, TestAction> {
            onIntent { intent ->
                val label = when (intent) {
                    first -> "first"
                    second -> "second"
                    else -> "other"
                }
                received += label
                intent
            }
        }
        val decorator = debounceIntentsDecorator<TestState, TestIntent, TestAction>(
            name = "DebounceIntents",
            timeoutSelector = { intent -> if (intent == first) timeout else ZERO }
        )

        "then the earlier pending delivery is canceled" {
            (decorator decorates child).test(TestState.Some) {
                onStart()
                first = TestIntent { }
                second = TestIntent { }

                onIntent(first)
                advanceBy(timeout / 2)

                onIntent(second) shouldBe second

                idle()
                received.shouldContainExactly("second")
                advanceBy(timeout)

                received.shouldContainExactly("second")
            }
        }
    }
})
