package pro.respawn.flowmvi.test.decorator

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import pro.respawn.flowmvi.annotation.ExperimentalFlowMVIAPI
import pro.respawn.flowmvi.decorator.decorates
import pro.respawn.flowmvi.decorators.intentTimeoutDecorator
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.plugins.timeTravelPlugin
import pro.respawn.flowmvi.test.plugin.test
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.advanceBy
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.testTimeTravel
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalFlowMVIAPI::class)
class IntentTimeoutDecoratorTest : FreeSpec({
    configure()

    "given child plugin that consumes intents immediately" - {
        val timeTravel = testTimeTravel()
        val plugin = timeTravelPlugin(timeTravel)
        val decorator = intentTimeoutDecorator<TestState, TestIntent, TestAction>(timeout = 10.milliseconds)

        "when intent completes within timeout" {
            (decorator decorates plugin).test(TestState.Some, timeTravel) {
                onStart()

                shouldNotThrowAny { onIntent(TestIntent { }) }
            }
        }
    }

    "given child plugin slower than timeout" - {
        val timeout = 10.milliseconds
        val fallback = TestIntent { }
        val plugin = plugin<TestState, TestIntent, TestAction> {
            onIntent { intent ->
                delay(timeout * 2)
                intent
            }
        }
        val decorator = intentTimeoutDecorator<TestState, TestIntent, TestAction>(
            timeout = timeout,
            onTimeout = { fallback }
        )

        "when timeout elapses" {
            (decorator decorates plugin).test(TestState.Some) {
                onStart()
                val original = TestIntent { }

                val result = async { onIntent(original) }

                advanceBy(timeout)

                result.await() shouldBe fallback
            }
        }
    }
})
