package pro.respawn.flowmvi.test.decorator

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.decorator.decorates
import pro.respawn.flowmvi.decorator.decorator
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.plugins.compositePlugin
import pro.respawn.flowmvi.plugins.timeTravelPlugin
import pro.respawn.flowmvi.test.plugin.test
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.TestState.SomeData
import pro.respawn.flowmvi.util.asUnconfined
import pro.respawn.flowmvi.util.testDecorator
import pro.respawn.flowmvi.util.testTimeTravel
import pro.respawn.flowmvi.util.withType
import sun.invoke.util.ValueConversions.ignore

class DecoratorValuesTest : FreeSpec({
    asUnconfined()
    val timeTravel = testTimeTravel()
    beforeEach {
        timeTravel.reset()
    }

    "given a plugin that modifies state" - {
        val plugin = plugin<TestState, TestIntent, TestAction> {
            onState { _, new ->
                new.withType<SomeData<Int>, _> {
                    copy(data = data + 1)
                }
            }
        }
        "and a decorator that calls the chain and returns the result" - {
            val decorator = testDecorator {
                onState { chain, old, new ->
                    chain.run {
                        old shouldBe TestState.Some
                        val result = onState(old, new)
                        result shouldBe SomeData(1)
                        result
                    }
                }
            }
            "then the final result is the new state" {
                (decorator decorates plugin).test(TestState.Some, timeTravel) {
                    val result = onState(TestState.Some, SomeData(0))
                    result shouldBe SomeData(1)
                }
            }
        }
        "and a decorator that replaces the final state" - {
            val replacement = SomeData(2)
            val decorator = testDecorator {
                onState { chain, old, new ->
                    chain.run { onState(old, new) }
                    replacement
                }
            }
            "then the final state is the one modified by the decorator" {
                (decorator decorates plugin).test(TestState.Some, timeTravel) {
                    val result = onState(TestState.Some, SomeData(0))
                    result shouldBe SomeData(2)
                }
            }
        }
        "and a decorator that ignores the chain" - {
            val decorator = testDecorator {
                onState { chain, old, new ->
                    old shouldBe TestState.Some
                    old
                }
            }
            "then the plugin is not invoked" {
                // we install TT directly on the plugin so that the test DSL itself is not affected, or it will hold
                // the decorated value
                val withTT = compositePlugin(listOf(plugin, timeTravelPlugin(timeTravel)))
                (decorator decorates withTT).test(TestState.Some) {
                    val result = onState(TestState.Some, SomeData(0))
                    result shouldBe TestState.Some
                    timeTravel.states.shouldBeEmpty()
                }
            }
        }
    }
})
