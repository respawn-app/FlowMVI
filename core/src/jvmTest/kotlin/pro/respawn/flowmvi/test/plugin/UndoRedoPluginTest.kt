package pro.respawn.flowmvi.test.plugin

import app.cash.turbine.test
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.plugins.undoRedoPlugin
import pro.respawn.flowmvi.test.subscribeAndTest
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.TestState.SomeData
import pro.respawn.flowmvi.util.asUnconfined
import pro.respawn.flowmvi.util.idle
import pro.respawn.flowmvi.util.testStore
import pro.respawn.flowmvi.util.testTimeTravelPlugin

class UndoRedoPluginTest : FreeSpec({
    asUnconfined()
    val timeTravel = testTimeTravelPlugin()
    beforeEach {
        timeTravel.reset()
    }
    "Given undo/redo plugin" - {
        val plugin = undoRedoPlugin<TestState, TestIntent, TestAction>(10)
        "and an intent that changes state" - {
            val intent = TestIntent {
                plugin(
                    redo = {
                        updateState<SomeData<Int>, _> {
                            copy(data = data + 1)
                        }
                    },
                ) {
                    updateState<SomeData<Int>, _> {
                        copy(data = data - 1)
                    }
                }
            }

            "and when intent is sent" - {
                "then it is executed" {
                    testStore(initial = SomeData(0), timeTravel = timeTravel) {
                        install(plugin)
                    }.subscribeAndTest {
                        intent(intent)
                        idle()
                        state shouldBe SomeData(1)
                        idle()
                        plugin.isQueueEmpty shouldBe false
                    }
                }
            }

            "and when undo is called" - {
                "then state is restored" {
                    testStore(initial = SomeData(0), timeTravel = timeTravel) {
                        install(plugin)
                    }.subscribeAndTest {
                        idle()
                        intent(intent)
                        idle()
                        plugin.undo(require = true)
                        idle()
                        plugin.index.value shouldBe -1
                        idle()
                        states.test {
                            awaitItem() shouldBe SomeData(0)
                        }
                    }
                }
            }
            "and multiple intents are sent" - {
                testStore(initial = SomeData(0), timeTravel = timeTravel) {
                    install(plugin)
                }.subscribeAndTest {
                    val reps = 5
                    repeat(5) {
                        intent(intent)
                    }
                    idle()
                    // "then queue size matches intent count"
                    plugin.index.value shouldBe reps - 1
                    state shouldBe SomeData(5)
                    // "then multiple actions can be undone"
                    plugin.undo(true)
                    plugin.undo(true)
                    idle()
                    plugin.index.value shouldBe reps - 1 - 2 // 2
                    plugin.queueSize shouldBe 5
                    // "then making another action replaces the redo queue"
                    intent(intent)
                    idle()
                    assertSoftly {
                        plugin.queueSize shouldBe 4
                        plugin.index.value shouldBe 3
                    }
                }
            }
        }
        "then plugin resets after store is stopped" {
            plugin.isQueueEmpty shouldBe true
        }
    }
})
