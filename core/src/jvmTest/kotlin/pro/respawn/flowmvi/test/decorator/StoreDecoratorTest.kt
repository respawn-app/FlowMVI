package pro.respawn.flowmvi.test.decorator

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.decorator.decoratedWith
import pro.respawn.flowmvi.decorator.decorates
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.plugins.timeTravelPlugin
import pro.respawn.flowmvi.test.plugin.test
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.testDecorator
import pro.respawn.flowmvi.util.testTimeTravel

class StoreDecoratorTest : FreeSpec({
    configure()

    val timeTravel = testTimeTravel()
    val timeTravelPlugin = timeTravelPlugin(timeTravel)
    afterEach {
        timeTravel.reset()
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
                onStart { chain ->
                    chain.run { onStart() }
                    ++invocations
                }
            }
            "then the decorator callback should still be executed" {
                (decorator decorates plugin).test(TestState.Some) {
                    onStart()
                    invocations shouldBe 1
                }
            }
        }
    }
    "given multiple decorators" - {
        val invocations = mutableListOf<Int>()
        val decorator1 = testDecorator {
            onStart { chain ->
                chain.run { onStart() }
                invocations += 1
            }
        }
        val decorator2 = testDecorator {
            onStart { chain ->
                chain.run { onStart() }
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
            onStart { chain ->
                chain.run { onStart() }
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
            onStart { it.run { onStart() } }
            onIntentEnqueue { chain, intent -> chain.run { onIntentEnqueue(intent) } }
            onIntent { chain, it -> chain.run { onIntent(it) } }
            onException { chain, it -> chain.run { onException(it) } }
            onState { chain, old, new -> chain.run { onState(old, new) } }
            onAction { chain, it -> chain.run { onAction(it) } }
            onActionDispatch { chain, action -> chain.run { onActionDispatch(action) } }
            onSubscribe { chain, it -> chain.run { onSubscribe(it) } }
            onUnsubscribe { chain, it -> chain.run { onUnsubscribe(it) } }
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
    "given decorator that wraps enqueue" - {
        val calls = mutableListOf<String>()
        val plugin = plugin<TestState, TestIntent, TestAction> {
            onIntentEnqueue { intent ->
                calls += "child:$intent"
                intent
            }
        }
        val decorator = testDecorator {
            onIntentEnqueue { chain, intent ->
                calls += "decorator:$intent"
                chain.run { onIntentEnqueue(intent) }
            }
        }
        "then decorator invokes child" {
            (decorator decorates plugin).test(TestState.Some) {
                val intent = TestIntent { }
                onIntentEnqueue(intent)
                calls shouldContainExactly listOf("decorator:$intent", "child:$intent")
            }
        }
    }
    "given decorator that wraps action dispatch" - {
        val calls = mutableListOf<String>()
        val plugin = plugin<TestState, TestIntent, TestAction> {
            onActionDispatch { action ->
                calls += "child:$action"
                action
            }
        }
        val decorator = testDecorator {
            onActionDispatch { chain, action ->
                calls += "decorator:$action"
                chain.run { onActionDispatch(action) }
            }
        }
        "then decorator invokes child" {
            (decorator decorates plugin).test(TestState.Some) {
                val action = TestAction.Some
                onActionDispatch(action)
                calls shouldContainExactly listOf("decorator:$action", "child:$action")
            }
        }
    }
})
