package pro.respawn.flowmvi.test.decorator

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.decorator.decorates
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.impl.plugin.asInstance
import pro.respawn.flowmvi.test.plugin.test
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.testDecorator

class WrapNotNullTest : FreeSpec({
    configure()

    "given plugin without handler" - {
        "when decorator provides wrapper" {
            var wrapperInvocations = 0
            val plugin = plugin<TestState, TestIntent, TestAction> { }
            val decorator = testDecorator {
                onStart { _ ->
                    wrapperInvocations++
                }
            }
            (decorator decorates plugin).test(TestState.Some) {
                onStart()
                wrapperInvocations shouldBe 1
            }
        }
        "when decorator absent" {
            val plugin = plugin<TestState, TestIntent, TestAction> { }
            val decorator = testDecorator { }
            val instance = (decorator decorates plugin).asInstance()
            instance.onStart shouldBe null
        }
    }

    "given plugin with handler" - {
        "when decorator invokes child" {
            var childInvocations = 0
            var wrapperInvocations = 0
            val plugin = plugin<TestState, TestIntent, TestAction> {
                onStart {
                    childInvocations++
                }
            }
            val decorator = testDecorator {
                onStart { child ->
                    child.run { onStart() }
                    wrapperInvocations++
                }
            }
            (decorator decorates plugin).test(TestState.Some) {
                onStart()
                wrapperInvocations shouldBe 1
                childInvocations shouldBe 1
            }
        }
        "when decorator absent" {
            var childInvocations = 0
            val plugin = plugin<TestState, TestIntent, TestAction> {
                onStart {
                    childInvocations++
                }
            }
            val decorator = testDecorator { }
            (decorator decorates plugin).test(TestState.Some) {
                onStart()
                childInvocations shouldBe 1
            }
        }
    }
})
