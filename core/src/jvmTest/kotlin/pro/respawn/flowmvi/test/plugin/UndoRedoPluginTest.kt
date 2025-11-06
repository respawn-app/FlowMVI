package pro.respawn.flowmvi.test.plugin

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import pro.respawn.flowmvi.plugins.UndoRedo
import pro.respawn.flowmvi.plugins.undoRedoPlugin
import pro.respawn.flowmvi.util.TestAction
import pro.respawn.flowmvi.util.TestIntent
import pro.respawn.flowmvi.util.TestState
import pro.respawn.flowmvi.util.configure
import pro.respawn.flowmvi.util.idle

private fun UndoRedo.shouldBeEmpty() {
    queueSize shouldBe 0
    canUndo shouldBe false
    canRedo shouldBe false
    index.value shouldBe -1
}

class UndoRedoPluginTest : FreeSpec({
    configure()
    "Given undo/redo" - {
        val plugin = UndoRedo(10)
        var counter = 0

        @IgnorableReturnValue
        suspend fun run() = plugin.invoke(true, redo = { ++counter }, undo = { --counter })
        "and when queue is empty" - {
            "then undo throws" {
                shouldThrowAny {
                    plugin.undo(true)
                }
            }
        }
        "and when redo is invoked" - {
            "then block is executed" {
                with(plugin) {
                    val _ = run()
                    counter shouldBe 1
                    isQueueEmpty shouldBe false
                    index.value shouldBe 0
                    canRedo shouldBe false
                    canUndo shouldBe true
                }
            }
        }

        "and when undo is called" - {
            "then state is restored" {
                with(plugin) {
                    plugin.undo(true)
                    counter shouldBe 0
                    isQueueEmpty shouldBe false
                    canRedo shouldBe true
                    canUndo shouldBe false
                    index.value shouldBe -1
                }
            }
        }
        "and when multiple operations are executed" - {
            plugin.reset()
            counter = 0
            val reps = 5
            repeat(5) { run() }
            "then queue size matches intent count" {
                plugin.index.value shouldBe reps - 1
                counter shouldBe 5
            }
            "then multiple actions can be undone" {
                plugin.undo(true)
                plugin.undo(true)
                idle()
                plugin.index.value shouldBe reps - 1 - 2 // 2
                counter shouldBe 3
                plugin.queueSize shouldBe 5
            }
            "then making another action replaces the redo queue" {
                run()
                idle()
                assertSoftly {
                    counter shouldBe 4
                    plugin.queueSize shouldBe 5 - 2 + 1
                    plugin.index.value shouldBe 4 - 1
                }
            }
            "then undone action can be redone" {
                plugin.undo(false)
                counter shouldBe 3
                plugin.redo(true)
                counter shouldBe 4
            }
        }
        "and undo/redo is installed as a plugin" - {
            undoRedoPlugin<TestState, TestIntent, TestAction>(plugin, resetOnException = true).test(TestState.Some) {
                "and an exception is thrown" - {
                    val e = IllegalArgumentException()
                    onException(e)
                    "then queue is cleared" {
                        timeTravel.exceptions shouldContain e
                        counter shouldBe 4
                        plugin.shouldBeEmpty()
                    }
                }
                "and store is closed" - {
                    run()
                    counter shouldBe 5
                    onStop(null)
                    "then queue is cleared" {
                        plugin.shouldBeEmpty()
                    }
                }
            }
        }
    }
})
