package pro.respawn.flowmvi.test.plugin

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.plugins.consumeIntentsPlugin
import pro.respawn.flowmvi.plugins.reducePlugin
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState

class ReducePluginTest : FreeSpec({
    "Given consume intents plugin" - {
        val plugin = consumeIntentsPlugin<TestState, TestIntent, TestAction>()
        plugin.test(TestState.Some) {
            "then intent is consumed" - {
                onIntent(TestIntent { }).shouldBeNull()
            }
        }
    }
    "Given reduce plugin that consumes intents" - {
        var invocations = 0
        val plugin = reducePlugin<TestState, TestIntent, TestAction> { ++invocations }
        plugin.test(TestState.Some) {
            "then reduce should invoke action on intent" - {
                val result = onIntent(TestIntent { })
                invocations shouldBe 1
                "and intent is consumed" {
                    result.shouldBeNull()
                }
            }
        }
    }
})
