package pro.respawn.flowmvi.test.plugin

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.api.StorePlugin
import pro.respawn.flowmvi.dsl.plugin
import pro.respawn.flowmvi.impl.plugin.PluginInstance
import pro.respawn.flowmvi.impl.plugin.compose
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.configure

class PluginCompositionExhaustiveTest : FreeSpec({
    configure()

    // this is childish but i don't see any other way to automatically verify we didn't forget to compose properly
    val hookNames = PluginInstance::class.java.declaredFields
        .map { it.name }
        .filter { it.startsWith("on") }
        .sorted()

    "given two plugin instances with all hooks set" - {
        "then composite plugin delegates every hook to all children" {
            val sentinel = plugin<TestState, TestIntent, TestAction> { }
            sentinel.test(initial = TestState.Some) {
                val calledBy = mutableMapOf<String, MutableSet<Int>>()
                fun record(hook: String, id: Int) {
                    calledBy.getOrPut(hook) { mutableSetOf() }.add(id)
                }

                fun instance(id: Int) = PluginInstance<TestState, TestIntent, TestAction>(
                    name = "p$id",
                    onState = { _, new ->
                        record("onState", id)
                        new
                    },
                    onIntentEnqueue = { intent ->
                        record("onIntentEnqueue", id)
                        intent
                    },
                    onIntent = { intent ->
                        record("onIntent", id)
                        intent
                    },
                    onAction = { action ->
                        record("onAction", id)
                        action
                    },
                    onActionDispatch = { action ->
                        record("onActionDispatch", id)
                        action
                    },
                    onException = { e ->
                        record("onException", id)
                        e
                    },
                    onStart = { record("onStart", id) },
                    onSubscribe = { _ -> record("onSubscribe", id) },
                    onUnsubscribe = { _ -> record("onUnsubscribe", id) },
                    onStop = { _ -> record("onStop", id) },
                    onUndeliveredIntent = { _ -> record("onUndeliveredIntent", id) },
                    onUndeliveredAction = { _ -> record("onUndeliveredAction", id) },
                )

                val composed: StorePlugin<TestState, TestIntent, TestAction> =
                    listOf(instance(1), instance(2)).compose(name = "composed")

                suspend fun invokeHook(name: String) {
                    when (name) {
                        "onState" -> composed.run { onState(TestState.Some, TestState.SomeData(0)) }
                        "onIntentEnqueue" -> composed.onIntentEnqueue(TestIntent { })
                        "onIntent" -> composed.run { onIntent(TestIntent { }) }
                        "onAction" -> composed.run { onAction(TestAction.Some) }
                        "onActionDispatch" -> composed.onActionDispatch(TestAction.Some)
                        "onException" -> composed.run { onException(IllegalStateException("boom")) }
                        "onStart" -> composed.run { onStart() }
                        "onSubscribe" -> composed.run { onSubscribe(1) }
                        "onUnsubscribe" -> composed.run { onUnsubscribe(0) }
                        "onStop" -> composed.run { onStop(null) }
                        "onUndeliveredIntent" -> composed.run { onUndeliveredIntent(TestIntent { }) }
                        "onUndeliveredAction" -> composed.run { onUndeliveredAction(TestAction.Some) }
                        else -> error("No invoker for hook $name. Update this test when adding new hooks.")
                    }
                }

                hookNames.forEach { hook ->
                    calledBy.remove(hook)
                    invokeHook(hook)
                    calledBy[hook] shouldBe setOf(1, 2)
                }
            }
        }
    }
})
