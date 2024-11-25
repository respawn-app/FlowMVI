package pro.respawn.flowmvi.test.decorator

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.decorator.decoratedWith
import pro.respawn.flowmvi.decorator.decorates
import pro.respawn.flowmvi.decorator.proceed
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.exceptions.NeverProceededException
import pro.respawn.flowmvi.plugins.timeTravelPlugin
import pro.respawn.flowmvi.test.plugin.test
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.asUnconfined
import pro.respawn.flowmvi.util.testDecorator
import pro.respawn.flowmvi.util.testTimeTravel

class StoreDecoratorTest : FreeSpec({
    asUnconfined()

    val timeTravel = testTimeTravel()
    val timeTravelPlugin = timeTravelPlugin(timeTravel)
    afterEach {
        timeTravel.reset()
    }
    "given a decorator that never calls proceed" - {
        val decorator = testDecorator {
            onStart { /* do nothing */ }
        }

        "then decorator throws" {
            (decorator decorates timeTravelPlugin).test(TestState.Some) {
                shouldThrow<NeverProceededException> {
                    onStart()
                }
                timeTravel.starts shouldBe 0
            }
        }
    }
    "given decorator that doesn't have a callback wrapper defined" - {
        val decorator = testDecorator {}
        "then the plugin is invoked directly" {
            (decorator decorates timeTravelPlugin).test(TestState.Some) {
                onStart()
                timeTravel.starts shouldBe 1
            }
        }
    }
    "given plugin that doesn't have a callback set" - {
        val plugin = plugin<TestState, TestIntent, TestAction> { }

        "and a decorator that defines a callback wrapper" - {
            var invocations = 0
            val decorator = testDecorator {
                onStart {
                    proceed()
                    ++invocations
                }
            }
            "then the decorator callback is skipped" {
                (decorator decorates plugin).test(TestState.Some) {
                    onStart()
                    invocations shouldBe 0
                }
            }
        }
    }
    "given multiple decorators" - {
        val invocations = mutableListOf<Int>()
        val decorator1 = testDecorator {
            onStart {
                proceed()
                invocations += 1
            }
        }
        val decorator2 = testDecorator {
            onStart {
                proceed()
                invocations += 2
            }
        }
        "then decorators are applied in the correct order" {
            (timeTravelPlugin decoratedWith listOf(decorator1, decorator2)).test(TestState.Some) {
                onStart()
                timeTravel.starts shouldBe 1
                invocations shouldContainExactly listOf(1, 2)
            }
        }
    }
    "given a decorator that invokes a callback" - {
        val invocations = mutableListOf<Int>()
        beforeEach { invocations.clear() }
        val decorator1 = testDecorator {
            onStart {
                proceed()
                invocations += 1
            }
        }
        "and another that does not" - {
            val decorator2 = testDecorator { }
            "then if the chain is invoked in direct order, callback is invoked" {
                (timeTravelPlugin decoratedWith listOf(decorator1, decorator2)).test(TestState.Some) {
                    onStart()
                    timeTravel.starts shouldBe 1
                    invocations shouldContainExactly listOf(1)
                }
            }
            "then if the chain is invoked in reverse order, callback is invoked" {
                (timeTravelPlugin decoratedWith listOf(decorator2, decorator1)).test(TestState.Some) {
                    onStart()
                    timeTravel.starts shouldBe 1
                    invocations shouldContainExactly listOf(1)
                }
            }
        }
    }
    "given a decorator with all callbacks defined" - {
        val decorator = testDecorator {
            onStart { proceed() }
            onIntent { proceed(it) }
            onException { proceed(it) }
            onState { _, new -> proceed(new) }
            onAction { proceed(it) }
            onSubscribe { proceed(it) }
            onUnsubscribe { proceed(it) }
        }
        "then all callbacks are invoked" {
            (timeTravelPlugin decoratedWith decorator).test(TestState.Some) {
                onStart()
                timeTravel.starts shouldBe 1
                onIntent(TestIntent { })
                timeTravel.intents shouldHaveSize 1
                onAction(TestAction.Some)
                timeTravel.actions shouldHaveSize 1
                onState(TestState.Some, TestState.Some)
                timeTravel.states shouldHaveSize 1
                onSubscribe(1)
                timeTravel.subscriptions shouldBe 1
                onUnsubscribe(1)
                timeTravel.unsubscriptions shouldBe 1
            }
        }
    }
})
