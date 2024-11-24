package pro.respawn.flowmvi.test.plugin

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.plugins.recoverPlugin
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState

class RecoverPluginTest : FreeSpec({
    "Given a recover plugin that swallows exceptions" - {
        var recovers = 0
        recoverPlugin<TestState, TestIntent, TestAction> {
            ++recovers
            null
        }.test(TestState.Some) {
            "And an exception is thrown" - {
                val exception = RuntimeException()
                val result = onException(exception)
                "then exception should be handled" {
                    timeTravel.exceptions.shouldBeEmpty()
                    result.shouldBeNull()
                    recovers shouldBe 1
                }
            }
        }
    }
})
